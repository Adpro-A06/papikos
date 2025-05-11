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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
    private String kosId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(kosRestController).build();

        penyewaUser = new User("penyewa@example.com", "password123!", Role.PENYEWA);
        pemilikUser = new User("pemilik@example.com", "password456!", Role.PEMILIK_KOS);

        kosId = UUID.randomUUID().toString();
        userId = penyewaUser.getId().toString();
        validToken = "valid-token";
        validAuthHeader = "Bearer " + validToken;
  
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
        kos2.setId(UUID.randomUUID().toString());
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
                .andExpect(jsonPath("$[0].id", is(kosId)))
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
                .andExpect(jsonPath("$[0].id", is(kosId)))
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
    void testGetKosDetailSuccess() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(eq(UUID.fromString(userId)))).thenReturn(penyewaUser);
        when(kosService.findById(kosId)).thenReturn(Optional.of(kos));

        mockMvc.perform(get("/api/kos/{id}", kosId)
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(kosId)))
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

        mockMvc.perform(get("/api/kos/{id}", kosId)
                .header("Authorization", validAuthHeader))
                .andExpect(status().isNotFound());
        verify(authService).decodeToken(validToken);
        verify(authService).findById(eq(UUID.fromString(userId)));
        verify(kosService).findById(kosId);
    }
}