package id.ac.ui.cs.advprog.papikos.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.papikos.authentication.dto.AuthDto;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthRestControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthRestController authRestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final String validEmail = "test@example.com";
    private final String validPassword = "P@ssword123";
    private final String validToken = "valid.jwt.token";
    private final UUID validUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authRestController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void registerSuccess() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail(validEmail);
        request.setPassword(validPassword);
        request.setRole("PENYEWA");

        User mockUser = new User(validEmail, validPassword, Role.PENYEWA);
        when(authService.registerUser(anyString(), anyString(), any(Role.class))).thenReturn(mockUser);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registrasi berhasil!"))
                .andExpect(jsonPath("$.data.email").value(validEmail))
                .andExpect(jsonPath("$.data.role").value("PENYEWA"));

        verify(authService).registerUser(validEmail, validPassword, Role.PENYEWA);
    }

    @Test
    void registerInvalidInput() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail(validEmail);
        request.setPassword(validPassword);
        request.setRole("ADMIN");

        when(authService.registerUser(anyString(), anyString(), any(Role.class)))
                .thenThrow(new IllegalArgumentException("Role tidak valid"));
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Role tidak valid"));
    }

    @Test
    void loginSuccess() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(validEmail);
        request.setPassword(validPassword);

        User mockUser = new User(validEmail, validPassword, Role.PENYEWA);
        when(authService.login(validEmail, validPassword)).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(validUserId.toString());
        when(authService.findById(validUserId)).thenReturn(mockUser);

        User spyUser = spy(mockUser);
        doReturn(validUserId).when(spyUser).getId();
        when(authService.findById(validUserId)).thenReturn(spyUser);
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(validToken))
                .andExpect(jsonPath("$.userId").value(validUserId.toString()))
                .andExpect(jsonPath("$.email").value(validEmail))
                .andExpect(jsonPath("$.role").value("PENYEWA"));

        verify(authService).login(validEmail, validPassword);
        verify(authService).decodeToken(validToken);
        verify(authService).findById(validUserId);
    }

    @Test
    void loginInvalidCredentials() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(validEmail);
        request.setPassword("wrongPassword");

        when(authService.login(validEmail, "wrongPassword"))
                .thenThrow(new RuntimeException("Email atau password salah"));
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email atau password salah"));

        verify(authService).login(validEmail, "wrongPassword");
        verifyNoMoreInteractions(authService);
    }

    @Test
    void logoutSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout berhasil"));

        verify(authService).logout(validToken);
    }

    @Test
    void logoutInvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "InvalidTokenFormat"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));

        verifyNoInteractions(authService);
    }

    @Test
    void logoutServiceException() throws Exception {
        doThrow(new RuntimeException("Token expired")).when(authService).logout(validToken);
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error saat logout: Token expired"));

        verify(authService).logout(validToken);
    }

    @Test
    void getUserInfoSuccess() throws Exception {
        User mockUser = new User(validEmail, validPassword, Role.PENYEWA);
        User spyUser = spy(mockUser);

        doReturn(validUserId).when(spyUser).getId();
        doReturn(true).when(spyUser).isApproved();
        when(authService.decodeToken(validToken)).thenReturn(validUserId.toString());
        when(authService.findById(validUserId)).thenReturn(spyUser);

        mockMvc.perform(get("/api/v1/auth/user")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Data user berhasil diambil"))
                .andExpect(jsonPath("$.data.id").value(validUserId.toString()))
                .andExpect(jsonPath("$.data.email").value(validEmail))
                .andExpect(jsonPath("$.data.role").value("PENYEWA"))
                .andExpect(jsonPath("$.data.approved").value(true));

        verify(authService).decodeToken(validToken);
        verify(authService).findById(validUserId);
    }

    @Test
    void getUserInfoInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/user")
                .header("Authorization", "InvalidTokenFormat"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));

        verifyNoInteractions(authService);
    }

    @Test
    void getUserInfoServiceException() throws Exception {
        when(authService.decodeToken(validToken)).thenThrow(new RuntimeException("Token tidak valid"));
        mockMvc.perform(get("/api/v1/auth/user")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));

        verify(authService).decodeToken(validToken);
        verifyNoMoreInteractions(authService);
    }
}