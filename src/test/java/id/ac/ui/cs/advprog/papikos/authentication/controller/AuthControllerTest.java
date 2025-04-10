package id.ac.ui.cs.advprog.papikos.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private ObjectMapper objectMapper;

    static class RegisterRequest {
        public String email;
        public String password;
        public String role;
        public RegisterRequest(String email, String password, String role) {
            this.email = email;
            this.password = password;
            this.role = role;
        }
        public RegisterRequest() {}
    }

    static class LoginRequest {
        public String email;
        public String password;
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
        public LoginRequest() {}
    }

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testRegisterPenyewa() throws Exception {
        String email = "penyewa@example.com";
        String password = "P@ssword123";
        Role role = Role.PENYEWA;
        User user = new User(email, password, role);
        when(authService.registerUser(eq(email), eq(password), eq(role))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, password, role.name()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value(role.name()));
    }

    @Test
    public void testRegisterPemilikKos() throws Exception {
        String email = "pemilik@example.com";
        String password = "Owner@456!";
        Role role = Role.PEMILIK_KOS;
        User user = new User(email, password, role);
        when(authService.registerUser(eq(email), eq(password), eq(role))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, password, role.name()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value(role.name()))
                .andExpect(jsonPath("$.approved").value(false));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        String email = "login@example.com";
        String password = "P@ssword123";
        String token = "jwt-0099e054-be40-4f09-a763-569b295d2e2b";
        when(authService.login(eq(email), eq(password))).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jwt-")));
    }

    @Test
    public void testLoginWrongInput() throws Exception {
        String email = "login2@example.com";
        when(authService.login(eq(email), eq("WrongP@ss!"))).thenThrow(new RuntimeException("Username atau password salah!"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "WrongP@ss!"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username atau password salah!")));
    }

    @Test
    public void testLoginLogout() throws Exception {
        String email = "logout@example.com";
        String password = "P@ssword123";
        Role role = Role.PENYEWA;
        User user = new User(email, password, role);
        String token = "jwt-0099e054-be40-4f09-a763-569b295d2e2b";
        when(authService.registerUser(eq(email), eq(password), eq(role))).thenReturn(user);
        when(authService.login(eq(email), eq(password))).thenReturn(token);
        doNothing().when(authService).logout(eq(token));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertNotNull(loginResponse);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logout berhasil!")));
    }

    @Test
    public void testApproveOwner() throws Exception {
        String email = "pemilikapprove@example.com";
        String password = "Owner@123";
        Role role = Role.PEMILIK_KOS;
        User owner = new User(email, password, role);
        UUID userId = owner.getId();
        when(authService.registerUser(eq(email), eq(password), eq(role))).thenReturn(owner);
        when(authService.approvePemilikKos(eq(userId))).thenReturn(true);

        mockMvc.perform(post("/api/auth/approve/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Akun pemilik kos telah disetujui!")));
    }
}