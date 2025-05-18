package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KosControllerTest {

    @Mock
    private KosService kosService;

    @Mock
    private AuthService authService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private KosController kosController;

    private User adminUser;
    private User pemilikUser;
    private User penyewaUser;
    private List<Kos> availableKos;
    private Kos kos;
    private String validToken;
    private String userId;
    private String kosId;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin@example.com", "password123!", Role.ADMIN);
        pemilikUser = new User("pemilik@example.com", "password456!", Role.PEMILIK_KOS);
        penyewaUser = new User("penyewa@example.com", "password789!", Role.PENYEWA);

        kosId = UUID.randomUUID().toString();
        
        kos = new Kos();
        kos.setId(kosId);
        kos.setNama("Kos Melati");
        kos.setAlamat("Jl. Kenanga No. 10");
        kos.setDeskripsi("Kos nyaman dekat kampus");
        kos.setHarga(1500000);
        kos.setJumlah(5);
        kos.setStatus("AVAILABLE");
        kos.setPemilik(pemilikUser);

        availableKos = new ArrayList<>();
        availableKos.add(kos);

        userId = penyewaUser.getId().toString();
        validToken = "jwt-validToken";
    }

    @Test
    void testSearchKosWhenUserNotLoggedIn() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);
        String viewName = kosController.searchKos("keyword", session, model, redirectAttributes);
        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    void testSearchKosNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);

        String viewName = kosController.searchKos("keyword", session, model, redirectAttributes);
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
    }

    @Test
    void testSearchKosWithKeyword() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(kosService.searchByKeyword("melati")).thenReturn(availableKos);

        String viewName = kosController.searchKos("melati", session, model, redirectAttributes);
        assertEquals("home/PenyewaHome", viewName);
        verify(kosService).searchByKeyword("melati");
        verify(model).addAttribute("kosList", availableKos);
        verify(model).addAttribute("keyword", "melati");
        verify(model).addAttribute("user", penyewaUser);
    }

    @Test
    void testSearchKosEmptyKeyword() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(kosService.findAllAvailable()).thenReturn(availableKos);

        String viewName = kosController.searchKos("", session, model, redirectAttributes);
        assertEquals("home/PenyewaHome", viewName);
        verify(kosService).findAllAvailable();
        verify(model).addAttribute("kosList", availableKos);
        verify(model).addAttribute("keyword", "");
        verify(model).addAttribute("user", penyewaUser);
    }

    @Test
    void testViewKosDetailWhenUserNotLoggedIn() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);
        String viewName = kosController.viewKosDetail(kosId, session, model, redirectAttributes);
        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    void testViewKosDetailNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(pemilikUser);

        String viewName = kosController.viewKosDetail(kosId, session, model, redirectAttributes);
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
    }

    @Test
    void testViewKosDetailFound() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(kosService.findById(kosId)).thenReturn(Optional.of(kos));

        String viewName = kosController.viewKosDetail(kosId, session, model, redirectAttributes);
        assertEquals("penyewaan/DetailKos", viewName);
        verify(kosService).findById(kosId);
        verify(model).addAttribute("kos", kos);
        verify(model).addAttribute("user", penyewaUser);
    }

    @Test
    void testViewKosDetailNotFound() {
        String nonExistentKosId = UUID.randomUUID().toString();
        
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(kosService.findById(nonExistentKosId)).thenReturn(Optional.empty());

        String viewName = kosController.viewKosDetail(nonExistentKosId, session, model, redirectAttributes);
        assertEquals("redirect:/penyewa/home", viewName);
        verify(kosService).findById(nonExistentKosId);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }
}