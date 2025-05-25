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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

@ExtendWith(MockitoExtension.class)
class PenyewaanRestControllerTest {

    @Mock
    private PenyewaanService penyewaanService;

    @Mock
    private KosService kosService;

    @Mock
    private AuthService authService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private PenyewaanRestController penyewaanRestController;

    private ObjectMapper objectMapper;

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
        invalidAuthHeader = "Bearer invalid-token";

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
    void testGetAllPenyewaanSuccess() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByPenyewa(penyewaUser)).thenReturn(CompletableFuture.completedFuture(penyewaanList));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getAllPenyewaan(validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(1, responseBody.size());
        verify(authService).decodeToken(validToken);
        verify(authService).findById(any(UUID.class));
        verify(penyewaanService).findByPenyewa(penyewaUser);
    }

    @Test
    void testGetAllPenyewaanUnauthorized() {
        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getAllPenyewaan("");
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(penyewaanService, never()).findByPenyewa(any());
    }

    @Test
    void testGetAllPenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getAllPenyewaan(invalidAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
        verify(authService).decodeToken(anyString());
        verify(penyewaanService, never()).findByPenyewa(any());
    }

    @Test
    void testGetAllPenyewaanNonPenyewa() {
        when(authService.decodeToken(validToken)).thenReturn(adminUser.getId().toString());
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getAllPenyewaan(validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Anda tidak memiliki akses ke resource ini");
        verify(penyewaanService, never()).findByPenyewa(any());
    }

    @Test
    void testGetAllPenyewaanWhenServiceException() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);

        CompletableFuture<List<Penyewaan>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Database error"));
        when(penyewaanService.findByPenyewa(penyewaUser)).thenReturn(future);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getAllPenyewaan(validAuthHeader);
        await().atMost(1, TimeUnit.SECONDS).until(result::hasResult);
        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Database error", body.get("message"));

        verify(authService).decodeToken(validToken);
        verify(authService).findById(penyewaUser.getId());
        verify(penyewaanService).findByPenyewa(penyewaUser);
    }

    @Test
    void testGetPenyewaanSuccess() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(penyewaan)));
        when(penyewaanService.isPenyewaanEditable(penyewaan)).thenReturn(true);
        when(penyewaanService.isPenyewaanCancellable(penyewaan)).thenReturn(true);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getPenyewaan(penyewaanId, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("isEditable"));
        assertEquals(true, responseBody.get("isCancellable"));
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
        verify(penyewaanService).isPenyewaanEditable(penyewaan);
        verify(penyewaanService).isPenyewaanCancellable(penyewaan);
    }

    @Test
    void testGetPenyewaanNotFound() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getPenyewaan(penyewaanId, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Penyewaan tidak ditemukan");
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
    }

    @Test
    void testGetPenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getPenyewaan(penyewaanId, invalidAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");

        verify(authService).decodeToken(anyString());
        verify(penyewaanService, never()).findByIdAndPenyewa(anyString(), any());
    }

    @Test
    void testGetPenyewaanNonPenyewa() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(pemilikUser);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getPenyewaan(penyewaanId, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Anda tidak memiliki akses ke resource ini");
        verify(authService).decodeToken(validToken);
        verify(authService).findById(UUID.fromString(userId));
        verify(penyewaanService, never()).findByIdAndPenyewa(anyString(), any());
    }

    @Test
    void testGetPenyewaanWhenServiceException() throws Exception {
        String penyewaanId = UUID.randomUUID().toString();
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);

        CompletableFuture<java.util.Optional<Penyewaan>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Database error"));
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser)).thenReturn(failedFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getPenyewaan(penyewaanId, validAuthHeader);
        await().atMost(1, TimeUnit.SECONDS).until(result::hasResult);
        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Database error", body.get("message"));

        verify(authService).decodeToken(validToken);
        verify(authService).findById(penyewaUser.getId());
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
    }

    @Test
    void testCreatePenyewaanSuccess() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser)))
                .thenReturn(CompletableFuture.completedFuture(penyewaan));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.createPenyewaan(kosId, penyewaan,
                bindingResult, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(penyewaan, response.getBody());
        verify(penyewaanService).createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser));
    }

    @Test
    void testCreatePenyewaanKosNotFound() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        CompletableFuture<Penyewaan> errorFuture = new CompletableFuture<>();
        errorFuture.completeExceptionally(
                new java.util.concurrent.CompletionException(new EntityNotFoundException("Kos tidak ditemukan")));
        when(penyewaanService.createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser)))
                .thenReturn(errorFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.createPenyewaan(kosId, penyewaan,
                bindingResult, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Kos tidak ditemukan");
        verify(penyewaanService).createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser));
    }

    @Test
    void testCreatePenyewaanAtIllegalArgumentException() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(penyewaUser);

        CompletableFuture<Penyewaan> errorFuture = new CompletableFuture<>();
        errorFuture.completeExceptionally(
                new java.util.concurrent.CompletionException(new IllegalArgumentException("Data tidak valid")));
        when(penyewaanService.createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser)))
                .thenReturn(errorFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.createPenyewaan(kosId, penyewaan,
                bindingResult, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Data tidak valid");
    }

    @Test
    void testCreatePenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.createPenyewaan(
                kosId, penyewaan, bindingResult, invalidAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
        verify(penyewaanService, never()).createPenyewaan(any(), anyString(), any());
    }

    @Test
    void testCreatePenyewaanNonPenyewa() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(pemilikUser);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.createPenyewaan(
                kosId, penyewaan, bindingResult, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Anda tidak memiliki akses ke resource ini");
        verify(penyewaanService, never()).createPenyewaan(any(), anyString(), any());
    }

    @Test
    void testCreatePenyewaanWhenServiceException() throws Exception {
        String kosId = UUID.randomUUID().toString();

        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);

        Penyewaan newPenyewaan = new Penyewaan();
        newPenyewaan.setNamaLengkap("John Doe");
        newPenyewaan.setNomorTelepon("08123456789");
        newPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(5));
        newPenyewaan.setDurasiSewa(3);

        CompletableFuture<Penyewaan> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Unexpected database error"));
        when(penyewaanService.createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser)))
                .thenReturn(failedFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.createPenyewaan(
                kosId, newPenyewaan, bindingResult, validAuthHeader);
        await().atMost(1, TimeUnit.SECONDS).until(result::hasResult);
        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Unexpected database error", body.get("message"));

        verify(authService).decodeToken(validToken);
        verify(authService).findById(penyewaUser.getId());
        verify(penyewaanService).createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser));
    }

    @Test
    void testUpdatePenyewaanSuccess() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser)))
                .thenReturn(CompletableFuture.completedFuture(penyewaan));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.updatePenyewaan(
                penyewaanId, penyewaan, bindingResult, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(penyewaan, response.getBody());
        verify(penyewaanService).updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));
    }

    @Test
    void testUpdatePenyewaanNotFound() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        CompletableFuture<Penyewaan> errorFuture = new CompletableFuture<>();
        errorFuture.completeExceptionally(
                new java.util.concurrent.CompletionException(new EntityNotFoundException("Penyewaan tidak ditemukan")));
        when(penyewaanService.updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser)))
                .thenReturn(errorFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.updatePenyewaan(
                penyewaanId, penyewaan, bindingResult, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Penyewaan tidak ditemukan");
        verify(penyewaanService).updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));
    }

    @Test
    void testUpdatePenyewaanAtIllegalStateException() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(penyewaUser);

        CompletableFuture<Penyewaan> errorFuture = new CompletableFuture<>();
        errorFuture.completeExceptionally(new java.util.concurrent.CompletionException(
                new IllegalStateException("Penyewaan tidak dapat diubah")));
        when(penyewaanService.updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser)))
                .thenReturn(errorFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.updatePenyewaan(
                penyewaanId, penyewaan, bindingResult, validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Penyewaan tidak dapat diubah");
        verify(penyewaanService).updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));
    }

    @Test
    void testUpdatePenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.updatePenyewaan(
                penyewaanId, penyewaan, bindingResult, invalidAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
        verify(authService).decodeToken(anyString());
        verify(penyewaanService, never()).updatePenyewaan(any(), anyString(), any());
    }

    @Test
    void testUpdatePenyewaanWhenServiceException() throws Exception {
        String penyewaanId = UUID.randomUUID().toString();

        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);

        Penyewaan updatedPenyewaan = new Penyewaan();
        updatedPenyewaan.setNamaLengkap("John Doe Updated");
        updatedPenyewaan.setNomorTelepon("08123456789");
        updatedPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(7));
        updatedPenyewaan.setDurasiSewa(4);

        CompletableFuture<Penyewaan> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Database connection error"));
        when(penyewaanService.updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser)))
                .thenReturn(failedFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.updatePenyewaan(
                penyewaanId, updatedPenyewaan, bindingResult, validAuthHeader);
        await().atMost(1, TimeUnit.SECONDS).until(result::hasResult);
        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Database connection error", body.get("message"));

        verify(authService).decodeToken(validToken);
        verify(authService).findById(penyewaUser.getId());
        verify(penyewaanService).updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));
    }

    @Test
    void testUpdatePenyewaanNoNPenyewa() throws Exception {
        String penyewaanId = UUID.randomUUID().toString();
        when(authService.decodeToken(validToken)).thenReturn(pemilikUser.getId().toString());
        when(authService.findById(pemilikUser.getId())).thenReturn(pemilikUser);

        Penyewaan updatedPenyewaan = new Penyewaan();
        updatedPenyewaan.setNamaLengkap("John Doe");
        updatedPenyewaan.setNomorTelepon("08123456789");
        updatedPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(5));
        updatedPenyewaan.setDurasiSewa(3);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.updatePenyewaan(
                penyewaanId, updatedPenyewaan, bindingResult, validAuthHeader);
        await().atMost(1, TimeUnit.SECONDS).until(result::hasResult);
        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Anda tidak memiliki akses ke resource ini", body.get("message"));

        verify(authService).decodeToken(validToken);
        verify(authService).findById(pemilikUser.getId());
        verifyNoInteractions(penyewaanService);
    }

    @Test
    void testCancelPenyewaanSuccess() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.cancelPenyewaan(penyewaanId, penyewaUser))
                .thenReturn(CompletableFuture.completedFuture(null));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.cancelPenyewaan(penyewaanId,
                validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Penyewaan berhasil dibatalkan", responseBody.get("message"));
        verify(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
    }

    @Test
    void testCancelPenyewaanNotFound() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        CompletableFuture<Void> errorFuture = new CompletableFuture<>();
        errorFuture.completeExceptionally(
                new java.util.concurrent.CompletionException(new EntityNotFoundException("Penyewaan tidak ditemukan")));
        when(penyewaanService.cancelPenyewaan(penyewaanId, penyewaUser))
                .thenReturn(errorFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.cancelPenyewaan(penyewaanId,
                validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Penyewaan tidak ditemukan");
        verify(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
    }

    @Test
    void testCancelPenyewaanAtIllegalStateException() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(penyewaUser);

        CompletableFuture<Void> errorFuture = new CompletableFuture<>();
        errorFuture.completeExceptionally(new java.util.concurrent.CompletionException(
                new IllegalStateException("Penyewaan tidak dapat dibatalkan")));
        when(penyewaanService.cancelPenyewaan(penyewaanId, penyewaUser))
                .thenReturn(errorFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.cancelPenyewaan(penyewaanId,
                validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Penyewaan tidak dapat dibatalkan");
        verify(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
    }

    @Test
    void testCancelPenyewaanWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.cancelPenyewaan(penyewaanId,
                invalidAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
        verify(authService).decodeToken(anyString());
        verify(penyewaanService, never()).cancelPenyewaan(anyString(), any());
    }

    @Test
    void testCancelPenyewaanNonPenyewa() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(pemilikUser);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.cancelPenyewaan(penyewaanId,
                validAuthHeader);
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Anda tidak memiliki akses ke resource ini");
        verify(authService).decodeToken(validToken);
        verify(authService).findById(UUID.fromString(userId));
        verify(penyewaanService, never()).cancelPenyewaan(anyString(), any());
    }

    @Test
    void testCancelPenyewaanWhenServiceException() {
        String penyewaanId = UUID.randomUUID().toString();

        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(penyewaUser.getId())).thenReturn(penyewaUser);

        CompletableFuture<Void> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Unexpected database error during cancellation"));
        when(penyewaanService.cancelPenyewaan(eq(penyewaanId), eq(penyewaUser))).thenReturn(failedFuture);

        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.cancelPenyewaan(
                penyewaanId, validAuthHeader);
        await().atMost(1, TimeUnit.SECONDS).until(result::hasResult);
        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Unexpected database error during cancellation", body.get("message"));

        // Verify service interactions
        verify(authService).decodeToken(validToken);
        verify(authService).findById(penyewaUser.getId());
        verify(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
    }

    @Test
    void testInvalidTokenFormat() {
        DeferredResult<ResponseEntity<?>> result = penyewaanRestController.getAllPenyewaan("InvalidFormat");
        while (!result.hasResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), "Token tidak valid");
        verify(penyewaanService, never()).findByPenyewa(any());
    }

    private void assertErrorResponse(Object responseBody, String expectedMessage) {
        assertTrue(responseBody instanceof Map);
        Map<?, ?> errorResponse = (Map<?, ?>) responseBody;
        assertEquals("error", errorResponse.get("status"));
        assertEquals(expectedMessage, errorResponse.get("message"));
    }
}