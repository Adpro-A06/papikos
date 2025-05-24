package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PengelolaanController.class)
@ExtendWith(SpringExtension.class)
public class PengelolaanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PengelolaanService pengelolaanService;

    @MockBean
    private AuthService authService;

    private User user;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        reset(pengelolaanService, authService);

        user = new User("test@example.com", "Password123!", Role.PEMILIK_KOS);
        user.setApproved(true);

        session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");

        when(authService.decodeToken("valid-token")).thenReturn(user.getId().toString());
        when(authService.findById(user.getId())).thenReturn(user);
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
        verify(authService, times(1)).findById(user.getId());
    }

    @Test
    void testShowAllKos() throws Exception {
        Kos kos = new Kos();
        kos.setId(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"));
        kos.setNama("Kos Test");
        kos.setUrlFoto("https://example.com/kos.jpg");
        kos.setPemilik(user);
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
        verify(authService, times(1)).findById(user.getId());
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
        kos.setPemilik(user);

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

        when(pengelolaanService.create(any(Kos.class))).thenReturn(CompletableFuture.completedFuture(createdKos));

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
        verify(authService, times(1)).findById(user.getId());
    }

    @Test
    void testUpdateKosPage() throws Exception {
        Kos kos = new Kos();
        kos.setId(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"));
        kos.setNama("Kos Test");
        kos.setUrlFoto("https://example.com/kos.jpg");
        kos.setPemilik(user);

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
        verify(authService, times(1)).findById(user.getId());
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
        updatedKos.setPemilik(user);

        when(pengelolaanService.update(any(Kos.class))).thenReturn(CompletableFuture.completedFuture(updatedKos));

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
        verify(authService, times(1)).findById(user.getId());
    }

    @Test
    void testUpdateKosPostNotFound() throws Exception {
        UUID id = UUID.fromString("66c08015-1179-4a7e-aea0-0f712171404c");
        when(pengelolaanService.update(any(Kos.class)))
                .thenReturn(CompletableFuture.failedFuture(new PengelolaanRepository.KosNotFoundException("Kos not found")));

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
        verify(authService, times(1)).findById(user.getId());
    }

    @Test
    void testDeleteKos() throws Exception {
        Kos kos = new Kos();
        kos.setId(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"));
        kos.setUrlFoto("https://example.com/kos.jpg");
        kos.setPemilik(user);

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
        verify(authService, times(1)).findById(user.getId());
    }
}