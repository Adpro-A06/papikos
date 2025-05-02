package id.ac.ui.cs.advprog.papikos.authentication.controller;

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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminApprovalControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminApprovalController adminApprovalController;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private User adminUser;
    private User nonAdminUser;
    private List<User> pendingOwners;
    private String validToken = "jwt-valid-token";
    private UUID validUserId;
    private String validUserIdStr;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminApprovalController).build();
        session = new MockHttpSession();
        validUserId = UUID.randomUUID();
        validUserIdStr = validUserId.toString();
 
        adminUser = new User("admin@example.com", "P@ssword123", Role.ADMIN);
        nonAdminUser = new User("user@example.com", "P@ssword123", Role.PENYEWA);
        User pendingOwner1 = new User("owner1@example.com", "P@ssword123", Role.PEMILIK_KOS);
        pendingOwner1.setApproved(false);
        User pendingOwner2 = new User("owner2@example.com", "P@ssword123", Role.PEMILIK_KOS);
        pendingOwner2.setApproved(false);
        pendingOwners = Arrays.asList(pendingOwner1, pendingOwner2);
    }

    @Test
    void adminNotLoggedInRedirectsToLogin() throws Exception {   
        mockMvc.perform(get("/admin/pending-approvals").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));
        verify(authService, never()).findAllPendingPemilikKos();
    }
    
    @Test
    void adminTokenDecodeException() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenThrow(new RuntimeException("Invalid token"));
        
        mockMvc.perform(get("/admin/pending-approvals").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));
        verify(authService, never()).findAllPendingPemilikKos();
    }
    
    @Test
    void userNotAdmin() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(validUserIdStr);
        when(authService.findById(any(UUID.class))).thenReturn(nonAdminUser);
        
        mockMvc.perform(get("/admin/pending-approvals").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("error", "Anda tidak memiliki akses ke halaman ini"));
        verify(authService, never()).findAllPendingPemilikKos();
    }
    
    @Test
    void adminViewWithPendingOwners() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(validUserIdStr);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);
        when(authService.findAllPendingPemilikKos()).thenReturn(pendingOwners);
        
        mockMvc.perform(get("/admin/pending-approvals").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/PendingApprovals"))
                .andExpect(model().attributeExists("pendingOwners"))
                .andExpect(model().attribute("pendingOwners", pendingOwners));
        verify(authService).findAllPendingPemilikKos();
    }

    @Test
    void adminApprovePemilikKosButNotLoggedIn() throws Exception {
        mockMvc.perform(post("/admin/approve")
                .param("userId", validUserIdStr)
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));
        verify(authService, never()).approvePemilikKos(any(UUID.class));
    }
    
    @Test
    void userNotAdminApprovePemilikKos() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(validUserIdStr);
        when(authService.findById(any(UUID.class))).thenReturn(nonAdminUser);
        
        mockMvc.perform(post("/admin/approve")
                .param("userId", validUserIdStr)
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attribute("error", "Anda tidak memiliki akses untuk melakukan tindakan ini"));
        verify(authService, never()).approvePemilikKos(any(UUID.class));
    }
    
    @Test
    void approvePemilikKosInvalidUUID() throws Exception {
        String invalidUUID = "not-a-uuid";
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(validUserIdStr);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);
        
        mockMvc.perform(post("/admin/approve")
                .param("userId", invalidUUID)
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pending-approvals"))
                .andExpect(flash().attribute("error", "Format ID tidak valid"));
        verify(authService, never()).approvePemilikKos(any(UUID.class));
    }
    
    @Test
    void approvePemilikKosThrowsException() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(validUserIdStr);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);
        doThrow(new RuntimeException("User tidak ditemukan")).when(authService).approvePemilikKos(any(UUID.class));
        
        mockMvc.perform(post("/admin/approve")
                .param("userId", validUserIdStr)
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pending-approvals"))
                .andExpect(flash().attribute("error", "User tidak ditemukan"));
        verify(authService).approvePemilikKos(any(UUID.class));
    }
    
    @Test
    void validApprovePemilikKos() throws Exception {
        session.setAttribute("JWT_TOKEN", validToken);
        when(authService.decodeToken(validToken)).thenReturn(validUserIdStr);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);
        doReturn(true).when(authService).approvePemilikKos(any(UUID.class));
        
        mockMvc.perform(post("/admin/approve")
                .param("userId", validUserIdStr)
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pending-approvals"))
                .andExpect(flash().attribute("success", "Pemilik kos berhasil disetujui"));
        verify(authService).approvePemilikKos(validUserId);
    }
}