package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KosRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KosService kosService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private KosRestController kosRestController;

    private User penyewaUser;
    private User pemilikUser;
    private Kos kos;
    private List<Kos> kosList;
    private String validToken;
    private String validAuthHeader;
    private String userId;
    private UUID kosId;
    private String kosIdString;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(kosRestController).build();

        penyewaUser = new User("penyewa@example.com", "password123!", Role.PENYEWA);
        pemilikUser = new User("pemilik@example.com", "password456!", Role.PEMILIK_KOS);

        kosId = UUID.randomUUID();
        kosIdString = kosId.toString();
        userId = penyewaUser.getId().toString();
        validToken = "valid-token";
        validAuthHeader = "Bearer " + validToken;
        invalidToken = "invalid-token";

        kos = new Kos();
        kos.setId(kosId);
        kos.setNama("Kos Melati");
        kos.setAlamat("Jl. Kenanga No. 10");
        kos.setDeskripsi("Kos nyaman dekat kampus");
        kos.setHarga(1500000);
        kos.setJumlah(5);
        kos.setStatus("AVAILABLE");
        kos.setPemilik(pemilikUser);

        kosList = new ArrayList<>();
        kosList.add(kos);

        Kos kos2 = new Kos();
        kos2.setId(UUID.randomUUID());
        kos2.setNama("Kos Mawar");
        kos2.setAlamat("Jl. Anggrek No. 15");
        kos2.setDeskripsi("Kos eksklusif dengan fasilitas lengkap");
        kos2.setHarga(2500000);
        kos2.setJumlah(3);
        kos2.setStatus("AVAILABLE");
        kos2.setPemilik(pemilikUser);
        kosList.add(kos2);
    }

    @Test
    void testGetAllKosSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(eq(UUID.fromString(userId)))).thenReturn(penyewaUser);
        when(kosService.findAllAvailable()).thenReturn(kosList);

        mockMvc.perform(get("/api/kos")
                        .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(kosIdString)))
                .andExpect(jsonPath("$[0].nama", is("Kos Melati")))
                .andExpect(jsonPath("$[1].nama", is("Kos Mawar")));
        verify(authService).decodeToken(validToken);
        verify(authService).findById(eq(UUID.fromString(userId)));
        verify(kosService).findAllAvailable();
    }

    @Test
    void testGetAllKosInvalidToken() throws Exception {
        mockMvc.perform(get("/api/kos")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("error")));
        verify(kosService, never()).findAllAvailable();
    }

    @Test
    void testGetAllKosNoAuthHeader() {
        ResponseEntity<?> response = kosRestController.getAllKos(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllKosWhenUserNotFound() {
        when(authService.decodeToken("valid-token")).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(null);
        ResponseEntity<?> response = kosRestController.getAllKos(validAuthHeader);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testSearchKosSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(eq(UUID.fromString(userId)))).thenReturn(penyewaUser);
        when(kosService.searchByKeyword("melati")).thenReturn(List.of(kos));

        mockMvc.perform(get("/api/kos/search")
                        .param("keyword", "melati")
                        .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(kosIdString)))
                .andExpect(jsonPath("$[0].nama", is("Kos Melati")));
        verify(authService).decodeToken(validToken);
        verify(authService).findById(eq(UUID.fromString(userId)));
        verify(kosService).searchByKeyword("melati");
    }

    @Test
    void testSearchKosEmptyKeyword() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(eq(UUID.fromString(userId)))).thenReturn(penyewaUser);
        when(kosService.findAllAvailable()).thenReturn(kosList);

        mockMvc.perform(get("/api/kos/search")
                        .param("keyword", "")
                        .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
        verify(authService).decodeToken(validToken);
        verify(authService).findById(eq(UUID.fromString(userId)));
        verify(kosService).findAllAvailable();
    }

    @Test
    void testSearchKosWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));
        ResponseEntity<?> response = kosRestController.searchKos("keyword", "Bearer " + invalidToken);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetKosDetailSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(eq(UUID.fromString(userId)))).thenReturn(penyewaUser);
        when(kosService.findById(kosId)).thenReturn(Optional.of(kos));

        mockMvc.perform(get("/api/kos/{id}", kosIdString)
                        .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(kosIdString)))
                .andExpect(jsonPath("$.nama", is("Kos Melati")));
        verify(authService).decodeToken(validToken);
        verify(authService).findById(eq(UUID.fromString(userId)));
        verify(kosService).findById(kosId);
    }

    @Test
    void testGetKosDetailNotFound() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(eq(UUID.fromString(userId)))).thenReturn(penyewaUser);
        when(kosService.findById(kosId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/kos/{id}", kosIdString)
                        .header("Authorization", validAuthHeader))
                .andExpect(status().isNotFound());
        verify(authService).decodeToken(validToken);
        verify(authService).findById(eq(UUID.fromString(userId)));
        verify(kosService).findById(kosId);
    }

    @Test
    void testGetKosDetailWhenInvalidToken() {
        when(authService.decodeToken(anyString())).thenThrow(new IllegalArgumentException("Token tidak valid"));
        ResponseEntity<?> response = kosRestController.getKosDetail(kosIdString, "Bearer " + invalidToken);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetKosDetailServiceException() {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(UUID.fromString(userId))).thenReturn(penyewaUser);
        when(kosService.findById(kosId)).thenThrow(new RuntimeException("Service error"));
        ResponseEntity<?> response = kosRestController.getKosDetail(kosIdString, validAuthHeader);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("Service error", errorResponse.get("message"));
    }
}