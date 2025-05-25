package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.kos.service.PengelolaanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(PengelolaanController.class)
@ExtendWith(SpringExtension.class)
public class PengelolaanControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PengelolaanService pengelolaanService;

        @MockBean
        private AuthService authService;

        private User pemilikUser;
        private User penyewaUser;
        private MockHttpSession session;
        private Penyewaan penyewaan;
        private List<Penyewaan> penyewaanList;
        private String validPenyewaanId;

        @BeforeEach
        void setUp() {
                reset(pengelolaanService, authService);

                pemilikUser = new User("test@example.com", "Password123!", Role.PEMILIK_KOS);
                pemilikUser.setApproved(true);

                penyewaUser = new User("penyewa@example.com", "Password123!", Role.PENYEWA);

                session = new MockHttpSession();
                session.setAttribute("JWT_TOKEN", "valid-token");

                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);

                Kos kos = new Kos();
                kos.setId(UUID.randomUUID());
                kos.setNama("Kos Test");
                kos.setPemilik(pemilikUser);

                validPenyewaanId = UUID.randomUUID().toString();
                penyewaan = new Penyewaan();
                penyewaan.setId(validPenyewaanId);
                penyewaan.setKos(kos);
                penyewaan.setPenyewa(penyewaUser);
                penyewaan.setNamaLengkap("John Doe");
                penyewaan.setNomorTelepon("08123456789");
                penyewaan.setStatus(StatusPenyewaan.PENDING);
                penyewaan.setTanggalCheckIn(LocalDate.now().plusDays(7));
                penyewaan.setDurasiSewa(3);
                penyewaan.setTotalBiaya(4500000);
                penyewaan.setWaktuPengajuan(LocalDateTime.now().minusDays(1));

                penyewaanList = Arrays.asList(penyewaan);
        }

        @Test
        void testCreateKosPage() throws Exception {
                MvcResult result = mockMvc.perform(get("/pemilik/create")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/CreateKos"))
                                .andExpect(model().attributeExists("kos"))
                                .andExpect(model().attribute("kos", instanceOf(Kos.class)));

                verify(authService, times(1)).decodeToken("valid-token");
                verify(authService, times(1)).findById(pemilikUser.getId());
        }

        @Test
        void testCreateKosPageNoAuthentication() throws Exception {
                session = new MockHttpSession();

                MvcResult result = mockMvc.perform(get("/pemilik/create")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attributeExists("error"));

                verifyNoInteractions(authService);
        }

        @Test
        void testCreateKosPageWrongRole() throws Exception {
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                MvcResult result = mockMvc.perform(get("/pemilik/create")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));

                verify(authService).findById(penyewaUser.getId());
                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testShowAllKos() throws Exception {
                Kos kos = new Kos();
                kos.setId(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"));
                kos.setNama("Kos Test");
                kos.setUrlFoto("https://example.com/kos.jpg");
                kos.setPemilik(pemilikUser);
                List<Kos> listKos = List.of(kos);

                when(pengelolaanService.findAll()).thenReturn(CompletableFuture.completedFuture(listKos));

                MvcResult result = mockMvc.perform(get("/pemilik/daftarkos")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/ListKos"))
                                .andExpect(model().attribute("allKos", hasSize(1)))
                                .andExpect(model().attribute("allKos", contains(hasProperty("nama", is("Kos Test")))));

                verify(pengelolaanService, times(1)).findAll();
                verify(authService, times(1)).decodeToken("valid-token");
                verify(authService, times(1)).findById(pemilikUser.getId());
        }

        @Test
        void testKosListPageNoAuthentication() throws Exception {
                session = new MockHttpSession();

                MvcResult result = mockMvc.perform(get("/pemilik/daftarkos")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"));

                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testKosListPageWrongRole() throws Exception {
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                MvcResult result = mockMvc.perform(get("/pemilik/daftarkos")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));

                verify(authService).findById(penyewaUser.getId());
                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testCreateKosPost() throws Exception {
                Kos kos = new Kos();
                kos.setNama("Kos Test");
                kos.setAlamat("Jl. Test");
                kos.setDeskripsi("Deskripsi Kos");
                kos.setJumlah(10);
                kos.setHarga(1000000);
                kos.setStatus("AVAILABLE");
                kos.setUrlFoto("https://example.com/kos.jpg");
                kos.setPemilik(pemilikUser);

                Kos createdKos = new Kos();
                createdKos.setId(UUID.randomUUID());
                createdKos.setNama(kos.getNama());
                createdKos.setAlamat(kos.getAlamat());
                createdKos.setDeskripsi(kos.getDeskripsi());
                createdKos.setJumlah(kos.getJumlah());
                createdKos.setHarga(kos.getHarga());
                createdKos.setStatus(kos.getStatus());
                createdKos.setUrlFoto(kos.getUrlFoto());
                createdKos.setPemilik(kos.getPemilik());

                when(pengelolaanService.create(any(Kos.class)))
                                .thenReturn(CompletableFuture.completedFuture(createdKos));

                MvcResult result = mockMvc.perform(post("/pemilik/create")
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", kos.getNama())
                                .param("alamat", kos.getAlamat())
                                .param("deskripsi", kos.getDeskripsi())
                                .param("jumlah", String.valueOf(kos.getJumlah()))
                                .param("harga", String.valueOf(kos.getHarga()))
                                .param("status", kos.getStatus())
                                .param("urlFoto", kos.getUrlFoto()))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("daftarkos"));

                ArgumentCaptor<Kos> kosCaptor = ArgumentCaptor.forClass(Kos.class);
                verify(pengelolaanService, times(1)).create(kosCaptor.capture());
                Kos capturedKos = kosCaptor.getValue();
                assertEquals(kos.getNama(), capturedKos.getNama());
                assertEquals(kos.getAlamat(), capturedKos.getAlamat());
                assertEquals(kos.getDeskripsi(), capturedKos.getDeskripsi());
                assertEquals(kos.getJumlah(), capturedKos.getJumlah());
                assertEquals(kos.getHarga(), capturedKos.getHarga(), 0.01);
                assertEquals(kos.getStatus(), capturedKos.getStatus());
                assertEquals(kos.getUrlFoto(), capturedKos.getUrlFoto());
                assertEquals(kos.getPemilik(), capturedKos.getPemilik());

                verify(authService, times(1)).decodeToken("valid-token");
                verify(authService, times(1)).findById(pemilikUser.getId());
        }

        @Test
        void testCreateKosPostNoAuthentication() throws Exception {
                session = new MockHttpSession();

                Kos testKos = new Kos();
                testKos.setNama("Kos Test");
                testKos.setAlamat("Jl. Test");
                testKos.setDeskripsi("Deskripsi Test");
                testKos.setJumlah(5);
                testKos.setHarga(1000000);
                testKos.setStatus("AVAILABLE");
                testKos.setUrlFoto("https://example.com/test.jpg");

                MvcResult result = mockMvc.perform(post("/pemilik/create")
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", testKos.getNama())
                                .param("alamat", testKos.getAlamat())
                                .param("deskripsi", testKos.getDeskripsi())
                                .param("jumlah", String.valueOf(testKos.getJumlah()))
                                .param("harga", String.valueOf(testKos.getHarga()))
                                .param("status", testKos.getStatus())
                                .param("urlFoto", testKos.getUrlFoto()))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"));

                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testCreateKosPostWrongRole() throws Exception {
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                Kos testKos = new Kos();
                testKos.setNama("Kos Test");
                testKos.setAlamat("Jl. Test");
                testKos.setDeskripsi("Deskripsi Test");
                testKos.setJumlah(5);
                testKos.setHarga(1000000);
                testKos.setStatus("AVAILABLE");
                testKos.setUrlFoto("https://example.com/test.jpg");

                MvcResult result = mockMvc.perform(post("/pemilik/create")
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", testKos.getNama())
                                .param("alamat", testKos.getAlamat())
                                .param("deskripsi", testKos.getDeskripsi())
                                .param("jumlah", String.valueOf(testKos.getJumlah()))
                                .param("harga", String.valueOf(testKos.getHarga()))
                                .param("status", testKos.getStatus())
                                .param("urlFoto", testKos.getUrlFoto()))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));

                verify(authService).findById(penyewaUser.getId());
                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testCreateKosPostWithValidationErrors() throws Exception {
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                MvcResult result = mockMvc.perform(post("/pemilik/create")
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", "")
                                .param("alamat", "Jl. Test")
                                .param("jumlah", "0")
                                .param("harga", "1000000"))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/CreateKos"))
                                .andExpect(model().attributeExists("kos"));

                verify(authService).findById(pemilikUser.getId());
                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testCreateKosPostServiceException() throws Exception {
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                Kos testKos = new Kos();
                testKos.setNama("Kos Test");
                testKos.setAlamat("Jl. Test");
                testKos.setDeskripsi("Deskripsi Test");
                testKos.setJumlah(5);
                testKos.setHarga(1000000);
                testKos.setStatus("AVAILABLE");
                testKos.setUrlFoto("https://example.com/test.jpg");

                CompletableFuture<Kos> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new RuntimeException("Database error"));
                when(pengelolaanService.create(any(Kos.class))).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(post("/pemilik/create")
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", testKos.getNama())
                                .param("alamat", testKos.getAlamat())
                                .param("deskripsi", testKos.getDeskripsi())
                                .param("jumlah", String.valueOf(testKos.getJumlah()))
                                .param("harga", String.valueOf(testKos.getHarga()))
                                .param("status", testKos.getStatus())
                                .param("urlFoto", testKos.getUrlFoto()))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/create"))
                                .andExpect(flash().attributeExists("error"));

                verify(authService).findById(pemilikUser.getId());
                verify(pengelolaanService).create(any(Kos.class));
        }

        @Test
        void testUpdateKosPage() throws Exception {
                Kos kos = new Kos();
                kos.setId(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"));
                kos.setNama("Kos Test");
                kos.setUrlFoto("https://example.com/kos.jpg");
                kos.setPemilik(pemilikUser);

                when(pengelolaanService.findById(kos.getId())).thenReturn(CompletableFuture.completedFuture(kos));

                MvcResult result = mockMvc.perform(get("/pemilik/edit/{id}", kos.getId().toString())
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/EditKos"))
                                .andExpect(model().attribute("kos", kos));

                verify(pengelolaanService, times(1)).findById(kos.getId());
                verify(authService, times(1)).decodeToken("valid-token");
                verify(authService, times(1)).findById(pemilikUser.getId());
        }

        @Test
        void testUpdateKosPageNoAuthentication() throws Exception {
                session = new MockHttpSession();
                UUID id = UUID.randomUUID();

                MvcResult result = mockMvc.perform(get("/pemilik/edit/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"));

                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testUpdateKosPageWrongRole() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                MvcResult result = mockMvc.perform(get("/pemilik/edit/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));

                verify(authService).findById(penyewaUser.getId());
                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testUpdateKosPageKosNotFoundException() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                CompletableFuture<Kos> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new PengelolaanRepository.KosNotFoundException("Kos not found"));
                when(pengelolaanService.findById(id)).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(get("/pemilik/edit/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/error/KosNotFound"));

                verify(authService).findById(pemilikUser.getId());
                verify(pengelolaanService).findById(id);
        }

        @Test
        void testUpdateKosPageOtherException() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                CompletableFuture<Kos> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new RuntimeException("Database connection failed"));
                when(pengelolaanService.findById(id)).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(get("/pemilik/edit/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarkos"))
                                .andExpect(flash().attributeExists("error"));

                verify(authService).findById(pemilikUser.getId());
                verify(pengelolaanService).findById(id);
        }

        @Test
        void testUpdateKosPost() throws Exception {
                Kos updatedKos = new Kos();
                updatedKos.setId(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"));
                updatedKos.setNama("Kos Updated");
                updatedKos.setAlamat("Jl. Updated");
                updatedKos.setDeskripsi("Deskripsi Updated");
                updatedKos.setJumlah(20);
                updatedKos.setHarga(2000000);
                updatedKos.setStatus("FULL");
                updatedKos.setUrlFoto("https://example.com/kos_updated.jpg");
                updatedKos.setPemilik(pemilikUser);

                when(pengelolaanService.update(any(Kos.class)))
                                .thenReturn(CompletableFuture.completedFuture(updatedKos));

                MvcResult result = mockMvc.perform(post("/pemilik/edit/{id}", updatedKos.getId().toString())
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", updatedKos.getNama())
                                .param("alamat", updatedKos.getAlamat())
                                .param("deskripsi", updatedKos.getDeskripsi())
                                .param("jumlah", String.valueOf(updatedKos.getJumlah()))
                                .param("harga", String.valueOf(updatedKos.getHarga()))
                                .param("status", updatedKos.getStatus())
                                .param("urlFoto", updatedKos.getUrlFoto()))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarkos"));

                ArgumentCaptor<Kos> kosCaptor = ArgumentCaptor.forClass(Kos.class);
                verify(pengelolaanService, times(1)).update(kosCaptor.capture());
                Kos capturedKos = kosCaptor.getValue();
                assertEquals(updatedKos.getId(), capturedKos.getId());
                assertEquals(updatedKos.getNama(), capturedKos.getNama());
                assertEquals(updatedKos.getAlamat(), capturedKos.getAlamat());
                assertEquals(updatedKos.getDeskripsi(), capturedKos.getDeskripsi());
                assertEquals(updatedKos.getJumlah(), capturedKos.getJumlah());
                assertEquals(updatedKos.getHarga(), capturedKos.getHarga(), 0.01);
                assertEquals(updatedKos.getStatus(), capturedKos.getStatus());
                assertEquals(updatedKos.getUrlFoto(), capturedKos.getUrlFoto());
                assertEquals(updatedKos.getPemilik(), capturedKos.getPemilik());

                verify(authService, times(1)).decodeToken("valid-token");
                verify(authService, times(1)).findById(pemilikUser.getId());
        }

        @Test
        void testUpdateKosPostNotFound() throws Exception {
                UUID id = UUID.fromString("66c08015-1179-4a7e-aea0-0f712171404c");
                when(pengelolaanService.update(any(Kos.class)))
                                .thenReturn(CompletableFuture.failedFuture(
                                                new PengelolaanRepository.KosNotFoundException("Kos not found")));

                MvcResult result = mockMvc.perform(post("/pemilik/edit/{id}", id.toString())
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", "Test Kos")
                                .param("alamat", "Jl. Test")
                                .param("deskripsi", "Deskripsi Kos")
                                .param("jumlah", "10")
                                .param("harga", "1000000")
                                .param("status", "AVAILABLE")
                                .param("urlFoto", "https://example.com/kos.jpg"))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/error/KosNotFound"));

                verify(pengelolaanService, times(1)).update(any(Kos.class));
                verify(authService, times(1)).decodeToken("valid-token");
                verify(authService, times(1)).findById(pemilikUser.getId());
        }

        @Test
        void testUpdateKosPostNoAuthentication() throws Exception {
                session = new MockHttpSession();
                UUID id = UUID.randomUUID();
                Kos editedKos = new Kos();
                editedKos.setNama("Kos Edit");
                editedKos.setAlamat("Jl. Edit Test");

                MvcResult result = mockMvc.perform(post("/pemilik/edit/{id}", id)
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", editedKos.getNama())
                                .param("alamat", editedKos.getAlamat())
                                .param("jumlah", "5")
                                .param("harga", "1500000"))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"));

                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testUpdateKosPostWrongRole() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                Kos editedKos = new Kos();
                editedKos.setNama("Kos Edit");
                editedKos.setAlamat("Jl. Edit Test");

                MvcResult result = mockMvc.perform(post("/pemilik/edit/{id}", id)
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", editedKos.getNama())
                                .param("alamat", editedKos.getAlamat())
                                .param("jumlah", "5")
                                .param("harga", "1500000"))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));

                verify(authService).findById(penyewaUser.getId());
                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testUpdateKosPostBindingErrors() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                MvcResult result = mockMvc.perform(post("/pemilik/edit/{id}", id)
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", "")
                                .param("alamat", "Jl. Edit")
                                .param("jumlah", "0")
                                .param("harga", "1500000"))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/EditKos"))
                                .andExpect(model().attributeExists("kos"));

                verify(authService).findById(pemilikUser.getId());
                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testUpdateKosPostOtherException() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                Kos editedKos = new Kos();
                editedKos.setNama("Kos Edit Test");
                editedKos.setAlamat("Jl. Edit Updated");
                editedKos.setDeskripsi("Deskripsi Updated");
                editedKos.setJumlah(5);
                editedKos.setHarga(1500000);
                editedKos.setStatus("AVAILABLE");

                CompletableFuture<Kos> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new RuntimeException("Database error"));
                when(pengelolaanService.update(any(Kos.class))).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(post("/pemilik/edit/{id}", id)
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("nama", editedKos.getNama())
                                .param("alamat", editedKos.getAlamat())
                                .param("deskripsi", editedKos.getDeskripsi())
                                .param("jumlah", String.valueOf(editedKos.getJumlah()))
                                .param("harga", String.valueOf(editedKos.getHarga()))
                                .param("status", editedKos.getStatus()) // Tambahkan parameter status
                                .param("urlFoto", "https://example.com/image.jpg")) // Tambahkan URL foto yang valid
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/edit/" + id))
                                .andExpect(flash().attributeExists("error"));

                verify(authService).findById(pemilikUser.getId());
                verify(pengelolaanService).update(any(Kos.class));
        }

        @Test
        void testDeleteKos() throws Exception {
                Kos kos = new Kos();
                kos.setId(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"));
                kos.setUrlFoto("https://example.com/kos.jpg");
                kos.setPemilik(pemilikUser);

                when(pengelolaanService.findById(kos.getId())).thenReturn(CompletableFuture.completedFuture(kos));
                when(pengelolaanService.delete(any(Kos.class))).thenReturn(CompletableFuture.completedFuture(null));

                MvcResult result = mockMvc.perform(post("/pemilik/delete/{id}", kos.getId().toString())
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarkos"));

                verify(pengelolaanService, times(1)).findById(kos.getId());
                verify(pengelolaanService, times(1)).delete(any(Kos.class));
                verify(authService, times(1)).decodeToken("valid-token");
                verify(authService, times(1)).findById(pemilikUser.getId());
        }

        @Test
        void testDeleteKosNoAuthentication() throws Exception {
                session = new MockHttpSession();
                UUID id = UUID.randomUUID();

                MvcResult result = mockMvc.perform(post("/pemilik/delete/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"));

                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testDeleteKosWrongRole() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                MvcResult result = mockMvc.perform(post("/pemilik/delete/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));

                verify(authService).findById(penyewaUser.getId());
                verifyNoInteractions(pengelolaanService);
        }

        @Test
        void testDeleteKosNotFoundException() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                CompletableFuture<Kos> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(
                                new PengelolaanRepository.KosNotFoundException("Kos tidak ditemukan"));
                when(pengelolaanService.findById(id)).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(post("/pemilik/delete/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/error/KosNotFound"))
                                .andExpect(model().attribute("errorMessage",
                                                "Kos dengan ID " + id + " tidak ditemukan."));

                verify(authService).findById(pemilikUser.getId());
                verify(pengelolaanService).findById(id);
                verify(pengelolaanService, never()).delete(any(Kos.class));
        }

        @Test
        void testDeleteKosOtherException() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                CompletableFuture<Kos> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new RuntimeException("Database error"));
                when(pengelolaanService.findById(id)).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(post("/pemilik/delete/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarkos"))
                                .andExpect(flash().attributeExists("error"));

                verify(authService).findById(pemilikUser.getId());
                verify(pengelolaanService).findById(id);
                verify(pengelolaanService, never()).delete(any(Kos.class));
        }

        @Test
        void testDeleteKosOperationException() throws Exception {
                UUID id = UUID.randomUUID();
                when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);
                when(authService.decodeToken("valid-token")).thenReturn(pemilikUser.getId().toString());

                Kos kos = new Kos();
                kos.setId(id);
                kos.setNama("Kos Delete Error Test");
                kos.setPemilik(pemilikUser);

                when(pengelolaanService.findById(id)).thenReturn(CompletableFuture.completedFuture(kos));

                CompletableFuture<Void> deleteFailedFuture = new CompletableFuture<>();
                deleteFailedFuture.completeExceptionally(new RuntimeException("Delete error"));
                when(pengelolaanService.delete(kos)).thenReturn(deleteFailedFuture);

                MvcResult result = mockMvc.perform(post("/pemilik/delete/{id}", id)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarkos"))
                                .andExpect(flash().attributeExists("error"));

                verify(authService).findById(pemilikUser.getId());
                verify(pengelolaanService).findById(id);
                verify(pengelolaanService).delete(kos);
        }

        @Test
        void testDaftarSewaSuccess() throws Exception {
                when(pengelolaanService.findAllSewa(pemilikUser.getId()))
                                .thenReturn(CompletableFuture.completedFuture(penyewaanList));

                MvcResult result = mockMvc.perform(get("/pemilik/daftarsewa")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().isOk())
                                .andExpect(view().name("pengelolaan/ListSewa"))
                                .andExpect(model().attribute("allSewa", penyewaanList))
                                .andExpect(model().attribute("user", pemilikUser));

                verify(pengelolaanService).findAllSewa(pemilikUser.getId());
        }

        @Test
        void testDaftarSewaNoAuthentication() throws Exception {
                session = new MockHttpSession();

                MvcResult result = mockMvc.perform(get("/pemilik/daftarsewa")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attributeExists("error"));

                verify(pengelolaanService, never()).findAllSewa(any());
        }

        @Test
        void testDaftarSewaWrongRole() throws Exception {
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                MvcResult result = mockMvc.perform(get("/pemilik/daftarsewa")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attributeExists("error"));

                verify(pengelolaanService, never()).findAllSewa(any());
        }

        @Test
        void testDaftarSewaServiceException() throws Exception {
                CompletableFuture<List<Penyewaan>> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new RuntimeException("Service error"));

                when(pengelolaanService.findAllSewa(pemilikUser.getId())).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(get("/pemilik/daftarsewa")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/home"))
                                .andExpect(flash().attributeExists("error"));

                verify(pengelolaanService).findAllSewa(pemilikUser.getId());
        }

        @Test
        void testAcceptPenyewaanSuccess() throws Exception {
                when(pengelolaanService.terimaSewa(validPenyewaanId, pemilikUser.getId()))
                                .thenReturn(CompletableFuture.completedFuture(null));

                MvcResult result = mockMvc.perform(post("/pemilik/ajuan-sewa/{id}", validPenyewaanId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarsewa"))
                                .andExpect(flash().attribute("success", "Penyewaan berhasil diterima"));

                verify(pengelolaanService).terimaSewa(validPenyewaanId, pemilikUser.getId());
        }

        @Test
        void testAcceptPenyewaanNoAuthentication() throws Exception {
                session = new MockHttpSession();

                MvcResult result = mockMvc.perform(post("/pemilik/ajuan-sewa/{id}", validPenyewaanId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"));

                verify(pengelolaanService, never()).terimaSewa(any(), any());
        }

        @Test
        void testAcceptPenyewaanWrongRole() throws Exception {
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                MvcResult result = mockMvc.perform(post("/pemilik/ajuan-sewa/{id}", validPenyewaanId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attributeExists("error"));

                verify(pengelolaanService, never()).terimaSewa(any(), any());
        }

        @Test
        void testAcceptPenyewaanInvalidId() throws Exception {
                String invalidId = "not-a-uuid";

                MvcResult result = mockMvc.perform(post("/pemilik/ajuan-sewa/{id}", invalidId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarsewa"))
                                .andExpect(flash().attribute("error", "ID penyewaan tidak valid"));

                verify(pengelolaanService, never()).terimaSewa(any(), any());
        }

        @Test
        void testAcceptPenyewaanServiceException() throws Exception {
                CompletableFuture<Void> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new RuntimeException("Service error"));

                when(pengelolaanService.terimaSewa(validPenyewaanId, pemilikUser.getId())).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(post("/pemilik/ajuan-sewa/{id}", validPenyewaanId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarsewa"))
                                .andExpect(flash().attributeExists("error"));

                verify(pengelolaanService).terimaSewa(validPenyewaanId, pemilikUser.getId());
        }

        @Test
        void testRejectPenyewaanSuccess() throws Exception {
                when(pengelolaanService.tolakSewa(validPenyewaanId, pemilikUser.getId()))
                                .thenReturn(CompletableFuture.completedFuture(null));

                MvcResult result = mockMvc.perform(post("/pemilik/tolak-sewa/{id}", validPenyewaanId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarsewa"))
                                .andExpect(flash().attribute("success", "Penyewaan berhasil ditolak"));

                verify(pengelolaanService).tolakSewa(validPenyewaanId, pemilikUser.getId());
        }

        @Test
        void testRejectPenyewaanNoAuthentication() throws Exception {
                session = new MockHttpSession();

                MvcResult result = mockMvc.perform(post("/pemilik/tolak-sewa/{id}", validPenyewaanId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"));

                verify(pengelolaanService, never()).tolakSewa(any(), any());
        }

        @Test
        void testRejectPenyewaanWrongRole() throws Exception {
                when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);
                when(authService.decodeToken("valid-token")).thenReturn(penyewaUser.getId().toString());

                MvcResult result = mockMvc.perform(post("/pemilik/tolak-sewa/{id}", validPenyewaanId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/api/auth/login"))
                                .andExpect(flash().attributeExists("error"));

                verify(pengelolaanService, never()).tolakSewa(any(), any());
        }

        @Test
        void testRejectPenyewaanInvalidId() throws Exception {
                String invalidId = "not-a-uuid";

                MvcResult result = mockMvc.perform(post("/pemilik/tolak-sewa/{id}", invalidId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarsewa"))
                                .andExpect(flash().attribute("error", "ID penyewaan tidak valid"));

                verify(pengelolaanService, never()).tolakSewa(any(), any());
        }

        @Test
        void testRejectPenyewaanServiceException() throws Exception {
                CompletableFuture<Void> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new RuntimeException("Service error"));

                when(pengelolaanService.tolakSewa(validPenyewaanId, pemilikUser.getId())).thenReturn(failedFuture);

                MvcResult result = mockMvc.perform(post("/pemilik/tolak-sewa/{id}", validPenyewaanId)
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(result))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/pemilik/daftarsewa"))
                                .andExpect(flash().attributeExists("error"));

                verify(pengelolaanService).tolakSewa(validPenyewaanId, pemilikUser.getId());
        }
}