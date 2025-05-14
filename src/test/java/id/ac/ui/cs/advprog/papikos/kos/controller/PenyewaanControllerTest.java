package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.kos.service.PenyewaanService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenyewaanControllerTest {

    @Mock
    private PenyewaanService penyewaanService;

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

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private PenyewaanController penyewaanController;

    private User penyewaUser;
    private User pemilikUser;
    private User adminUser;
    private Kos kos;
    private Penyewaan penyewaan;
    private List<Penyewaan> penyewaanList;
    private String validToken;
    private String userId;
    private String kosId;
    private String penyewaanId;

    @BeforeEach
    void setUp() {
        penyewaUser = new User("penyewa@example.com", "password123!", Role.PENYEWA);
        pemilikUser = new User("pemilik@example.com", "password456!", Role.PEMILIK_KOS);
        adminUser = new User("admin@example.com", "password789!", Role.ADMIN);

        kosId = UUID.randomUUID().toString();
        penyewaanId = UUID.randomUUID().toString();
        userId = penyewaUser.getId().toString();
        validToken = "jwt-validToken";

        kos = new Kos();
        kos.setId(kosId);
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
    void testListPenyewaanWhenUserNotLoggedIn() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);
        String viewName = penyewaanController.listPenyewaan(session, model, redirectAttributes);
        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(penyewaanService, never()).findByPenyewa(any(User.class));
    }

    @Test
    void testListPenyewaanNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);

        String viewName = penyewaanController.listPenyewaan(session, model, redirectAttributes);
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
        verify(penyewaanService, never()).findByPenyewa(any(User.class));
    }

    @Test
    void testListPenyewaanSuccess() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByPenyewa(penyewaUser)).thenReturn(penyewaanList);

        String viewName = penyewaanController.listPenyewaan(session, model, redirectAttributes);
        assertEquals("penyewaan/ListSewa", viewName);
        verify(penyewaanService).findByPenyewa(penyewaUser);
        verify(model).addAttribute("penyewaanList", penyewaanList);
        verify(model).addAttribute("user", penyewaUser);
    }

    @Test
    void testNewPenyewaanFormWhenUserNotLoggedIn() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);
        String viewName = penyewaanController.newPenyewaanForm(kosId, session, model, redirectAttributes);
        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(kosService, never()).findById(anyString());
    }

    @Test
    void testNewPenyewaanFormNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(pemilikUser);

        String viewName = penyewaanController.newPenyewaanForm(kosId, session, model, redirectAttributes);
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
        verify(kosService, never()).findById(anyString());
    }

    @Test
    void testNewPenyewaanFormKosNotFound() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(kosService.findById(kosId)).thenReturn(Optional.empty());

        String viewName = penyewaanController.newPenyewaanForm(kosId, session, model, redirectAttributes);
        assertEquals("redirect:/penyewa/home", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(kosService).findById(kosId);
    }

    @Test
    void testNewPenyewaanFormSuccess() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(kosService.findById(kosId)).thenReturn(Optional.of(kos));

        String viewName = penyewaanController.newPenyewaanForm(kosId, session, model, redirectAttributes);
        assertEquals("penyewaan/FormSewa", viewName);
        verify(kosService).findById(kosId);
        verify(model).addAttribute(eq("kos"), any(Kos.class));
        verify(model).addAttribute(eq("penyewaan"), any(Penyewaan.class));
        verify(model).addAttribute("user", penyewaUser);
    }

    @Test
    void testCreatePenyewaanNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(pemilikUser);

        String viewName = penyewaanController.createPenyewaan(
                kosId,
                "John Doe",
                "08123456789",
                LocalDate.now().plusDays(7),
                3,
                session,
                redirectAttributes);

        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
        verify(penyewaanService, never()).createPenyewaan(any(), anyString(), any());
    }

    @Test
    void testCreatePenyewaanSuccess() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        Penyewaan expectedPenyewaan = new Penyewaan();
        expectedPenyewaan.setNamaLengkap("John Doe");
        expectedPenyewaan.setNomorTelepon("08123456789");
        LocalDate checkInDate = LocalDate.now().plusDays(7);
        expectedPenyewaan.setTanggalCheckIn(checkInDate);
        expectedPenyewaan.setDurasiSewa(3);

        when(penyewaanService.createPenyewaan(argThat(penyewaan -> penyewaan.getNamaLengkap().equals("John Doe") &&
                penyewaan.getNomorTelepon().equals("08123456789") &&
                penyewaan.getTanggalCheckIn().equals(checkInDate) &&
                penyewaan.getDurasiSewa() == 3), eq(kosId), eq(penyewaUser))).thenReturn(penyewaan);

        String viewName = penyewaanController.createPenyewaan(
                kosId,
                "John Doe",
                "08123456789",
                checkInDate,
                3,
                session,
                redirectAttributes);

        assertEquals("redirect:/penyewaan/", viewName);
        verify(penyewaanService).createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser));
        verify(redirectAttributes).addFlashAttribute("success", "Pengajuan penyewaan berhasil dibuat");
    }

    @Test
    void testCreatePenyewaanServiceException() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        doThrow(new RuntimeException("Test error")).when(penyewaanService)
                .createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser));

        String viewName = penyewaanController.createPenyewaan(
                kosId,
                "John Doe",
                "08123456789",
                LocalDate.now().plusDays(7),
                3,
                session,
                redirectAttributes);

        assertEquals("redirect:/penyewaan/new/" + kosId, viewName);
        verify(penyewaanService).createPenyewaan(any(Penyewaan.class), eq(kosId), eq(penyewaUser));
        verify(redirectAttributes).addFlashAttribute("error", "Test error");
    }

    @Test
    void testEditPenyewaanFormWhenUserNotLoggedIn() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);
        String viewName = penyewaanController.editPenyewaanForm(penyewaanId, session, model, redirectAttributes);
        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(penyewaanService, never()).findByIdAndPenyewa(anyString(), any(User.class));
    }

    @Test
    void testEditPenyewaanFormNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);

        String viewName = penyewaanController.editPenyewaanForm(penyewaanId, session, model, redirectAttributes);
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
        verify(penyewaanService, never()).findByIdAndPenyewa(anyString(), any(User.class));
    }

    @Test
    void testEditPenyewaanFormNotFound() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser)).thenReturn(Optional.empty());

        String viewName = penyewaanController.editPenyewaanForm(penyewaanId, session, model, redirectAttributes);
        assertEquals("redirect:/penyewaan/", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
    }

    @Test
    void testEditPenyewaanFormNotEditable() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser)).thenReturn(Optional.of(penyewaan));
        when(penyewaanService.isPenyewaanEditable(penyewaan)).thenReturn(false);

        String viewName = penyewaanController.editPenyewaanForm(penyewaanId, session, model, redirectAttributes);
        assertEquals("redirect:/penyewaan/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Penyewaan tidak dapat diedit");
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
        verify(penyewaanService).isPenyewaanEditable(penyewaan);
    }

    @Test
    void testEditPenyewaanFormSuccess() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser)).thenReturn(Optional.of(penyewaan));
        when(penyewaanService.isPenyewaanEditable(penyewaan)).thenReturn(true);

        String viewName = penyewaanController.editPenyewaanForm(penyewaanId, session, model, redirectAttributes);
        assertEquals("penyewaan/EditSewa", viewName);
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
        verify(penyewaanService).isPenyewaanEditable(penyewaan);
        verify(model).addAttribute("penyewaan", penyewaan);
        verify(model).addAttribute("kos", kos);
        verify(model).addAttribute("user", penyewaUser);
    }

    @Test
    void testUpdatePenyewaanWhenUserNotLoggedIn() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        LocalDate checkInDate = LocalDate.now().plusDays(7);
        String viewName = penyewaanController.updatePenyewaan(
                penyewaanId,
                "John Doe",
                "08123456789",
                checkInDate,
                3,
                session,
                redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(penyewaanService, never()).updatePenyewaan(any(), anyString(), any());
    }

    @Test
    void testUpdatePenyewaanNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);

        LocalDate checkInDate = LocalDate.now().plusDays(7);
        String viewName = penyewaanController.updatePenyewaan(
                penyewaanId,
                "John Doe",
                "08123456789",
                checkInDate,
                3,
                session,
                redirectAttributes);

        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
        verify(penyewaanService, never()).updatePenyewaan(any(), anyString(), any());
    }

    @Test
    void testUpdatePenyewaanSuccess() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        LocalDate checkInDate = LocalDate.now().plusDays(7);

        when(penyewaanService.updatePenyewaan(argThat(penyewaan -> penyewaan.getNamaLengkap().equals("John Doe") &&
                penyewaan.getNomorTelepon().equals("08123456789") &&
                penyewaan.getTanggalCheckIn().equals(checkInDate) &&
                penyewaan.getDurasiSewa() == 3), eq(penyewaanId), eq(penyewaUser))).thenReturn(penyewaan);

        String viewName = penyewaanController.updatePenyewaan(
                penyewaanId,
                "John Doe",
                "08123456789",
                checkInDate,
                3,
                session,
                redirectAttributes);

        assertEquals("redirect:/penyewaan/", viewName);
        verify(penyewaanService).updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));
        verify(redirectAttributes).addFlashAttribute("success", "Penyewaan berhasil diperbarui");
    }

    @Test
    void testUpdatePenyewaanServiceException() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);

        LocalDate checkInDate = LocalDate.now().plusDays(7);

        doThrow(new RuntimeException("Test error")).when(penyewaanService)
                .updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));

        String viewName = penyewaanController.updatePenyewaan(
                penyewaanId,
                "John Doe",
                "08123456789",
                checkInDate,
                3,
                session,
                redirectAttributes);

        assertEquals("redirect:/penyewaan/" + penyewaanId + "/edit", viewName);
        verify(penyewaanService).updatePenyewaan(any(Penyewaan.class), eq(penyewaanId), eq(penyewaUser));
        verify(redirectAttributes).addFlashAttribute("error", "Test error");
    }

    @Test
    void testViewPenyewaanWhenUserNotLoggedIn() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);
        String viewName = penyewaanController.viewPenyewaan(penyewaanId, session, model, redirectAttributes);
        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(penyewaanService, never()).findByIdAndPenyewa(anyString(), any(User.class));
    }

    @Test
    void testViewPenyewaanNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);

        String viewName = penyewaanController.viewPenyewaan(penyewaanId, session, model, redirectAttributes);
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
        verify(penyewaanService, never()).findByIdAndPenyewa(anyString(), any(User.class));
    }

    @Test
    void testViewPenyewaanNotFound() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser)).thenReturn(Optional.empty());

        String viewName = penyewaanController.viewPenyewaan(penyewaanId, session, model, redirectAttributes);
        assertEquals("redirect:/penyewaan/", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
    }

    @Test
    void testViewPenyewaanSuccess() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        when(penyewaanService.findByIdAndPenyewa(penyewaanId, penyewaUser)).thenReturn(Optional.of(penyewaan));
        when(penyewaanService.isPenyewaanEditable(penyewaan)).thenReturn(true);
        when(penyewaanService.isPenyewaanCancellable(penyewaan)).thenReturn(true);

        String viewName = penyewaanController.viewPenyewaan(penyewaanId, session, model, redirectAttributes);
        assertEquals("penyewaan/DetailSewa", viewName);
        verify(penyewaanService).findByIdAndPenyewa(penyewaanId, penyewaUser);
        verify(penyewaanService).isPenyewaanEditable(penyewaan);
        verify(penyewaanService).isPenyewaanCancellable(penyewaan);
        verify(model).addAttribute("penyewaan", penyewaan);
        verify(model).addAttribute("isEditable", true);
        verify(model).addAttribute("isCancellable", true);
        verify(model).addAttribute("user", penyewaUser);
    }

    @Test
    void testCancelPenyewaanWhenUserNotLoggedIn() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);
        String viewName = penyewaanController.cancelPenyewaan(penyewaanId, session, redirectAttributes);
        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(penyewaanService, never()).cancelPenyewaan(anyString(), any(User.class));
    }

    @Test
    void testCancelPenyewaanNonPenyewa() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(adminUser);

        String viewName = penyewaanController.cancelPenyewaan(penyewaanId, session, redirectAttributes);
        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
        verify(penyewaanService, never()).cancelPenyewaan(anyString(), any(User.class));
    }

    @Test
    void testCancelPenyewaanSuccess() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        doNothing().when(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);

        String viewName = penyewaanController.cancelPenyewaan(penyewaanId, session, redirectAttributes);
        assertEquals("redirect:/penyewaan/", viewName);
        verify(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
        verify(redirectAttributes).addFlashAttribute("success", "Penyewaan berhasil dibatalkan");
    }

    @Test
    void testCancelPenyewaanServiceException() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(validToken);
        when(authService.decodeToken(validToken)).thenReturn(userId);
        when(authService.findById(any(UUID.class))).thenReturn(penyewaUser);
        doThrow(new RuntimeException("Test error")).when(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);

        String viewName = penyewaanController.cancelPenyewaan(penyewaanId, session, redirectAttributes);
        assertEquals("redirect:/penyewaan/" + penyewaanId, viewName);
        verify(penyewaanService).cancelPenyewaan(penyewaanId, penyewaUser);
        verify(redirectAttributes).addFlashAttribute("error", "Test error");
    }
}