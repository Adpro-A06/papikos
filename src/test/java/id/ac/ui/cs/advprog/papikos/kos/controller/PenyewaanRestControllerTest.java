package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.kos.service.PenyewaanService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PenyewaanRestControllerTest {

    private MockMvc mockMvc;

    @Mock(lenient = true)
    private PenyewaanService penyewaanService;

    @Mock(lenient = true)
    private KosService kosService;

    @Mock(lenient = true)
    private AuthService authService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private PenyewaanRestController penyewaanRestController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User penyewaUser;
    private User pemilikUser;
    private User adminUser;
    private Kos kos;
    private Penyewaan penyewaan;
    private List<Penyewaan> penyewaanList;
    private String validToken;
    private String validAuthHeader;
    private String invalidAuthHeader;
    private String userId;
    private String kosId;
    private String penyewaanId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(penyewaanRestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        penyewaUser = new User("penyewa@example.com", "password123!", Role.PENYEWA);
        pemilikUser = new User("pemilik@example.com", "password456!", Role.PEMILIK_KOS);
        adminUser = new User("admin@example.com", "password789!", Role.ADMIN);

        kosId = UUID.randomUUID().toString();
        penyewaanId = UUID.randomUUID().toString();
        userId = penyewaUser.getId().toString();
        validToken = "valid-token";
        validAuthHeader = "Bearer " + validToken;
        invalidAuthHeader = "invalid-token";

        kos = new Kos();
        kos.setId(UUID.fromString(kosId));
        kos.setNama("Kos Melati");
        kos.setAlamat("Jl. Kenanga No. 10");
        kos.setDeskripsi("Kos nyaman dekat kampus");
        kos.setHarga(1500000);
        kos.setJumlah(5);
        kos.setStatus("AVAILABLE");
        kos.setPemilik(pemilikUser);

        penyewaan = new Penyewaan();
        penyewaan.setId(penyewaanId);
        penyewaan.setKos(kos);
        penyewaan.setPenyewa(penyewaUser);
        penyewaan.setNamaLengkap("John Doe");
        penyewaan.setNomorTelepon("08123456789");
        penyewaan.setTanggalCheckIn(LocalDate.now().plusDays(7));
        penyewaan.setDurasiSewa(3);
        penyewaan.setTotalBiaya(4500000);
        penyewaan.setStatus(StatusPenyewaan.PENDING);
        penyewaan.setWaktuPengajuan(LocalDateTime.now());

        penyewaanList = new ArrayList<>();
        penyewaanList.add(penyewaan);
    }

    @Test
    void testGetAllPenyewaanSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByPenyewa(penyewaUser)).thenReturn(penyewaanList);

        mockMvc.perform(get("/api/penyewaan")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(penyewaanId)));  
        verify(authService).decodeToken(validToken);
        verify(authService).findById(any(UUID.class));
        verify(penyewaanService).findByPenyewa(penyewaUser);
    }

    @Test
    void testGetAllPenyewaanUnauthorized() throws Exception {
        mockMvc.perform(get("/api/penyewaan")
            .header("Authorization", ""))
            .andExpect(status().isUnauthorized());
        verify(penyewaanService, never()).findByPenyewa(any());
    }

    @Test
    void testGetAllPenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));
        ResponseEntity<?> response = penyewaanRestController.getAllPenyewaan(invalidAuthHeader);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
    }

    @Test
    void testGetAllPenyewaanNonPenyewa() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(adminUser.getId().toString());
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);
        
        mockMvc.perform(get("/api/penyewaan")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Anda tidak memiliki akses ke resource ini")));
        verify(penyewaanService, never()).findByPenyewa(any());
    }

    @Test
    void testGetPenyewaanSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser)).thenReturn(Optional.of(penyewaan));
        when(penyewaanService.isPenyewaanEditable(penyewaan)).thenReturn(true);
        when(penyewaanService.isPenyewaanCancellable(penyewaan)).thenReturn(true);
        
        mockMvc.perform(get("/api/penyewaan/{id}", penyewaanId)
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.penyewaan.id", is(penyewaanId)))
                .andExpect(jsonPath("$.isEditable", is(true)))
                .andExpect(jsonPath("$.isCancellable", is(true)));
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
        verify(penyewaanService).isPenyewaanEditable(penyewaan);
        verify(penyewaanService).isPenyewaanCancellable(penyewaan);
    }

    @Test
    void testGetPenyewaanNotFound() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/penyewaan/{id}", penyewaanId)
                .header("Authorization", validAuthHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Penyewaan tidak ditemukan")));
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
    }

    @Test
    void testGetPenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));
        ResponseEntity<?> response = penyewaanRestController.getPenyewaan(penyewaanId, invalidAuthHeader);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
    }

    @Test
    void testGetPenyewaanNonPenyewa() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(pemilikUser);
        ResponseEntity<?> response = penyewaanRestController.getPenyewaan(penyewaanId, validAuthHeader);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Anda tidak memiliki akses ke resource ini");
    }

    @Test
    void testCreatePenyewaanSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        Penyewaan newPenyewaan = new Penyewaan();
        newPenyewaan.setNamaLengkap("Jane Smith");
        newPenyewaan.setNomorTelepon("08987654321");
        newPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(5));
        newPenyewaan.setDurasiSewa(2);

        when(penyewaanService.createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser)))
                .thenReturn(penyewaan);

        mockMvc.perform(post("/api/penyewaan/kos/{kosId}", kosId)
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPenyewaan)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(penyewaanId)));
        verify(penyewaanService).createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser));
    }

    @Test
    void testCreatePenyewaanKosNotFound() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        Penyewaan newPenyewaan = new Penyewaan();
        newPenyewaan.setNamaLengkap("Jane Smith");
        newPenyewaan.setNomorTelepon("08987654321");
        newPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(5));
        newPenyewaan.setDurasiSewa(2);

        when(penyewaanService.createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser)))
                .thenThrow(new EntityNotFoundException("Kos tidak ditemukan"));

        mockMvc.perform(post("/api/penyewaan/kos/{kosId}", kosId)
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPenyewaan)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Kos tidak ditemukan")));
        verify(penyewaanService).createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser));
    }

    @Test
    void testCreatePenyewaanAtIllegalArgumentException() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(penyewaUser);
        when(penyewaanService.createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser)))
                .thenThrow(new IllegalArgumentException("Data tidak valid"));

        ResponseEntity<?> response = penyewaanRestController.createPenyewaan(
                kosId, penyewaan, bindingResult, validAuthHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Data tidak valid");
    }

    @Test
    void testCreatePenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));
        ResponseEntity<?> response = penyewaanRestController.createPenyewaan(
                kosId, penyewaan, bindingResult, invalidAuthHeader);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
    }

    @Test
    void testCreatePenyewaanNonPenyewa() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(pemilikUser);
        ResponseEntity<?> response = penyewaanRestController.createPenyewaan(
                kosId, penyewaan, bindingResult, validAuthHeader);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Anda tidak memiliki akses ke resource ini");
    }

    @Test
    void testUpdatePenyewaanSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        Penyewaan updatedPenyewaan = new Penyewaan();
        updatedPenyewaan.setNamaLengkap("Updated Name");
        updatedPenyewaan.setNomorTelepon("08567891234");
        updatedPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(10));
        updatedPenyewaan.setDurasiSewa(4);

        when(penyewaanService.updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser)))
                .thenReturn(penyewaan);

        mockMvc.perform(put("/api/penyewaan/{id}", penyewaanId)
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPenyewaan)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(penyewaanId)));
        verify(penyewaanService).updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));
    }

    @Test
    void testUpdatePenyewaanNotFound() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        Penyewaan updatedPenyewaan = new Penyewaan();
        updatedPenyewaan.setNamaLengkap("Updated Name");
        updatedPenyewaan.setNomorTelepon("08567891234");
        updatedPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(10));
        updatedPenyewaan.setDurasiSewa(4);

        when(penyewaanService.updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser)))
                .thenThrow(new EntityNotFoundException("Penyewaan tidak ditemukan"));

        mockMvc.perform(put("/api/penyewaan/{id}", penyewaanId)
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPenyewaan)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Penyewaan tidak ditemukan")));
        verify(penyewaanService).updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));
    }

    @Test
    void testUpdatePenyewaanAtIllegalStateException() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(penyewaUser);
        when(penyewaanService.updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser)))
                .thenThrow(new IllegalStateException("Penyewaan tidak dapat diubah"));

        ResponseEntity<?> response = penyewaanRestController.updatePenyewaan(
                penyewaanId, penyewaan, bindingResult, validAuthHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Penyewaan tidak dapat diubah");
    }

    @Test
    void testUpdatePenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));
        ResponseEntity<?> response = penyewaanRestController.updatePenyewaan(
                penyewaanId, penyewaan, bindingResult, invalidAuthHeader);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
    }

    @Test
    void testCancelPenyewaanSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        doNothing().when(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
        
        mockMvc.perform(delete("/api/penyewaan/{id}", penyewaanId)
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Penyewaan berhasil dibatalkan")));  
        verify(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
    }

    @Test
    void testCancelPenyewaanNotFound() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        doThrow(new EntityNotFoundException("Penyewaan tidak ditemukan"))
                .when(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
        
        mockMvc.perform(delete("/api/penyewaan/{id}", penyewaanId)
                .header("Authorization", validAuthHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Penyewaan tidak ditemukan")));
        verify(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
    }

    @Test
    void testCancelPenyewaanAtIllegalStateException() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(penyewaUser);
        doThrow(new IllegalStateException("Penyewaan tidak dapat dibatalkan"))
                .when(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);

        ResponseEntity<?> response = penyewaanRestController.cancelPenyewaan(penyewaanId, validAuthHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Penyewaan tidak dapat dibatalkan");
    }

    @Test
    void testCancelPenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));
        ResponseEntity<?> response = penyewaanRestController.cancelPenyewaan(penyewaanId, invalidAuthHeader);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
    }

    @Test
    void testCancelPenyewaanNonPenyewa() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(pemilikUser);
        ResponseEntity<?> response = penyewaanRestController.cancelPenyewaan(penyewaanId, validAuthHeader);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Anda tidak memiliki akses ke resource ini");
    }

    @Test
    void testInvalidTokenFormat() throws Exception {
        mockMvc.perform(get("/api/penyewaan")
                .header("Authorization", "InvalidFormat"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Token tidak valid")));
        verify(penyewaanService, never()).findByPenyewa(any());
    }

    private void assertErrorResponse(Object responseBody, String expectedMessage) {
        assertTrue(responseBody instanceof Map);
        Map<String, Object> errorResponse = (Map<String, Object>) responseBody;
        assertEquals("error", errorResponse.get("status"));
        assertEquals(expectedMessage, errorResponse.get("message"));
    }
}