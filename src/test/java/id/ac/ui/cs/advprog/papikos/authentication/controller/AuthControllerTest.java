package id.ac.ui.cs.advprog.papikos.authentication.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    public void testShowRegisterForm() throws Exception {
        mockMvc.perform(get("/api/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/RegisterPage"));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        String email = "test@example.com";
        String password = "P@ssword123";
        String role = "PENYEWA";
        
        User mockUser = new User(email, password, Role.PENYEWA);
        when(authService.registerUser(eq(email), eq(password), any(Role.class))).thenReturn(mockUser);
        
        mockMvc.perform(post("/api/auth/register")
                .param("email", email)
                .param("password", password)
                .param("role", role))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("success", "Registrasi berhasil! Silakan login."));
    }

    @Test
    public void testRegisterError() throws Exception {
        String email = "test@example.com";
        String password = "weak";
        String role = "PENYEWA";
        
        when(authService.registerUser(anyString(), anyString(), any(Role.class)))
                .thenThrow(new IllegalArgumentException("Password tidak memenuhi syarat"));
        
        mockMvc.perform(post("/api/auth/register")
                .param("email", email)
                .param("password", password)
                .param("role", role))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/register"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    public void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/LoginPage"));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        String email = "test@example.com";
        String password = "P@ssword123";
        String token = "jwt-abc123";
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.PENYEWA);
        
        when(authService.login(eq(email), eq(password))).thenReturn(token);
        when(authService.decodeToken(eq(token))).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(mockUser);
        
        mockMvc.perform(post("/api/auth/login")
                .param("email", email)
                .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/penyewa/home"));
    }

    @Test
    public void testLoginAsPemilik() throws Exception {
        String email = "pemilik@example.com";
        String password = "P@ssword123";
        String token = "jwt-abc123";
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.PEMILIK_KOS);
        
        when(authService.login(eq(email), eq(password))).thenReturn(token);
        when(authService.decodeToken(eq(token))).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(mockUser);
        
        mockMvc.perform(post("/api/auth/login")
                .param("email", email)
                .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pemilik/home"));
    }
    
    @Test
    public void testLoginAsAdmin() throws Exception {
        String email = "admin@example.com";
        String password = "P@ssword123";
        String token = "jwt-abc123";
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.ADMIN);
        
        when(authService.login(eq(email), eq(password))).thenReturn(token);
        when(authService.decodeToken(eq(token))).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(mockUser);
        
        mockMvc.perform(post("/api/auth/login")
                .param("email", email)
                .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/home"));
    }

    @Test
    public void testLoginError() throws Exception {
        String email = "test@example.com";
        String password = "WrongPassword";
        
        when(authService.login(eq(email), eq(password)))
                .thenThrow(new RuntimeException("Username atau password salah!"));
        
        mockMvc.perform(post("/api/auth/login")
                .param("email", email)
                .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    public void testLogoutSuccess() throws Exception {
        String token = "jwt-abc123";
        
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", token);
        
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("success", "Anda telah logout."));
        
        verify(authService).logout(eq(token));
    }

    @Test
    public void testLogoutNoSession() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("success", "Anda telah logout."));
        
        verify(authService, never()).logout(anyString());
    }

    @Test
    public void testLogoutError() throws Exception {
        String token = "jwt-invalid";
        
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", token);
        
        doThrow(new RuntimeException("Token tidak valid")).when(authService).logout(eq(token));
        
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("success", "Anda telah logout."));
        
        verify(authService).logout(eq(token));
    }
}