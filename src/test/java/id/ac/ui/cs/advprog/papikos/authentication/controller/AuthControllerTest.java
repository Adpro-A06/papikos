package id.ac.ui.cs.advprog.papikos.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        public RegisterRequest() {
        }
    }

    static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public LoginRequest() {
        }
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
        Role role = Role.PENYEWA;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest(email, password, role.name()))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jwt-")));
    }

    @Test
    public void testLoginWrongInput() throws Exception {
        String email = "login2@example.com";
        String password = "P@ssword123";
        Role role = Role.PENYEWA;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest(email, password, role.name()))))
                .andExpect(status().isCreated());

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

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest(email, password, role.name()))))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertNotNull(loginResponse);

        doNothing().when(authService).logout(eq(loginResponse));
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + loginResponse))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logout berhasil!")));
    }

    @Test
    public void testLogoutFailed() throws Exception {
        String invalidToken = "invalid-token";
        doThrow(new RuntimeException("Token tidak valid atau sudah logout!"))
                .when(authService).logout(eq(invalidToken));

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Token tidak valid atau sudah logout!")));
    }

    @Test
    public void testApprovePemilikKosSuccess() throws Exception {
        String email = "pemilikapprove@example.com";
        String password = "Owner@123";
        Role role = Role.PEMILIK_KOS;

        String register = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest(email, password, role.name()))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User registeredPemilikKos = objectMapper.readValue(register, User.class);
        UUID userId = registeredPemilikKos.getId();

        when(authService.approvePemilikKos(eq(userId))).thenReturn(true);
        mockMvc.perform(post("/api/auth/approve/" + userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Akun pemilik kos telah disetujui!")));
    }

    @Test
    public void testLoginPage() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/LoginPage"));
    }

    @Test
    public void testRegisterPage() throws Exception {
        mockMvc.perform(get("/api/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/RegisterPage"));
    }
}