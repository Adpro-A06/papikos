package id.ac.ui.cs.advprog.papikos.authentication.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private User adminUser;
    private User pemilikKosUser;
    private User pemilikKosBelumApproveUser;
    private User penyewaUser;
    private String validToken = "jwt-abc123";
    private UUID userId = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
        session = new MockHttpSession();
 
        adminUser = new User("admin@example.com", "P@ssword123", Role.ADMIN);
        pemilikKosUser = new User("pemilik@example.com", "P@ssword123", Role.PEMILIK_KOS);
        pemilikKosUser.setApproved(true);
        pemilikKosBelumApproveUser = new User("pemilik2@example.com", "P@ssword123", Role.PEMILIK_KOS);
        pemilikKosBelumApproveUser.setApproved(false);
        penyewaUser = new User("penyewa@example.com", "P@ssword123", Role.PENYEWA);
    }

    @Test
    public void testIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"));
    }
    
    @Test
    public void testAdminHomeLoggedInAsAdmin() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(adminUser);
 
        mockMvc.perform(get("/admin/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("home/AdminHome"));
    }
    
    @Test
    public void testAdminHomeNotLoggedIn() throws Exception {
        mockMvc.perform(get("/admin/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));
                
        verify(authService, never()).decodeToken(anyString());
    }
    
    @Test
    public void testAdminHomeLoggedInAsNotAdmin() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(penyewaUser);
   
        mockMvc.perform(get("/admin/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));
    }
    
    @Test
    public void testAdminHomeTokenInvalid() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/admin/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("error", "Sesi login Anda telah berakhir. Silakan login kembali."));
    }
    
    @Test
    public void testPemilikKosHomeLoggedInAsPemilik() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(pemilikKosUser);

        mockMvc.perform(get("/pemilik/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("home/PemilikKosHome"));
    }
    
    @Test
    public void testPemilikKosHomeNotLoggedIn() throws Exception {
        mockMvc.perform(get("/pemilik/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));
    }
    
    @Test
    public void testPemilikKosHomeLoggedInAsNotPemilik() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(penyewaUser);

        mockMvc.perform(get("/pemilik/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));
    }
    
    @Test
    public void testPenyewaHomeLoggedInAsPenyewa() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(penyewaUser);

        mockMvc.perform(get("/penyewa/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("home/PenyewaHome"));
    }
    
    @Test
    public void testPenyewaHomeNotLoggedIn() throws Exception {
        mockMvc.perform(get("/penyewa/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));
    }
    
    @Test
    public void testPenyewaHomeLoggedInAsNotPenyewa() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(adminUser);

        mockMvc.perform(get("/penyewa/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));
    }
}