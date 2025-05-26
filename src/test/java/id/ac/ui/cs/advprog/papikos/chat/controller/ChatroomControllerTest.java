package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.service.ChatroomService;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatroomControllerTest {

    @Mock
    private ChatroomService chatroomService;

    @Mock
    private AuthService authService;

    @Mock
    private KosService kosService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ChatroomController chatroomController;

    private User renterUser;
    private User ownerUser;
    private User adminUser;
    private Chatroom chatroom;
    private Kos kos;
    private UUID renterId;
    private UUID ownerId;
    private UUID adminId;
    private UUID chatroomId;
    private UUID propertyId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        renterId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        chatroomId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        jwtToken = "mock-jwt-token";

        renterUser = new User("renter@example.com", "Password123!", Role.PENYEWA);
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(renterUser, renterId);
        } catch (Exception e) {
        }

        ownerUser = new User("owner@example.com", "Password123!", Role.PEMILIK_KOS);
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(ownerUser, ownerId);
        } catch (Exception e) {
        }

        adminUser = new User("admin@example.com", "Password123!", Role.ADMIN);
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(adminUser, adminId);
        } catch (Exception e) {
        }

        chatroom = new Chatroom();
        chatroom.setId(chatroomId);
        chatroom.setRenterId(renterId);
        chatroom.setOwnerId(ownerId);
        chatroom.setPropertyId(propertyId);
        chatroom.setCreatedAt(LocalDateTime.now());

        kos = new Kos();
        kos.setId(propertyId);
        kos.setNama("Test Kos");
    }

    @Test
    void getChatroomsByRenterId_WithValidRenter_ShouldReturnChatroomList() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomsByRenterId(renterId)).thenReturn(Arrays.asList(chatroom));
        when(authService.findById(ownerId)).thenReturn(ownerUser);
        when(kosService.findById(propertyId)).thenReturn(Optional.of(kos));

        String result = chatroomController.getChatroomsByRenterId(renterId, session, model, redirectAttributes);

        assertEquals("chat/ChatroomList", result);
        verify(model).addAttribute(eq("chatrooms"), anyList());
        verify(model).addAttribute(eq("chatroomData"), any(Map.class));
        verify(model).addAttribute("user", renterUser);
        verify(chatroomService).getChatroomsByRenterId(renterId);
    }

    @Test
    void getChatroomsByRenterId_WithoutLogin_ShouldRedirectToLogin() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        String result = chatroomController.getChatroomsByRenterId(renterId, session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", result);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
    }

    @Test
    void getChatroomsByRenterId_WithWrongRole_ShouldRedirectWithError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(ownerId.toString());
        when(authService.findById(ownerId)).thenReturn(ownerUser);

        String result = chatroomController.getChatroomsByRenterId(renterId, session, model, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
    }

    @Test
    void getChatroomsByRenterId_WithWrongUserId_ShouldRedirectWithError() {
        UUID differentRenterId = UUID.randomUUID();
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);

        String result = chatroomController.getChatroomsByRenterId(differentRenterId, session, model, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
    }

    @Test
    void getChatroomsByOwnerId_WithValidOwner_ShouldReturnChatroomList() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(ownerId.toString());
        when(authService.findById(ownerId)).thenReturn(ownerUser);
        when(chatroomService.getChatroomsByOwnerId(ownerId)).thenReturn(Arrays.asList(chatroom));
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(kosService.findById(propertyId)).thenReturn(Optional.of(kos));

        String result = chatroomController.getChatroomsByOwnerId(ownerId, session, model, redirectAttributes);

        assertEquals("chat/ChatroomList", result);
        verify(model).addAttribute(eq("chatrooms"), anyList());
        verify(model).addAttribute(eq("chatroomData"), any(Map.class));
        verify(model).addAttribute("user", ownerUser);
        verify(chatroomService).getChatroomsByOwnerId(ownerId);
    }

    @Test
    void getChatroomsByOwnerId_WithoutLogin_ShouldRedirectToLogin() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        String result = chatroomController.getChatroomsByOwnerId(ownerId, session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", result);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
    }

    @Test
    void getChatroomsByOwnerId_WithWrongRole_ShouldRedirectWithError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);

        String result = chatroomController.getChatroomsByOwnerId(ownerId, session, model, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
    }

    @Test
    void getChatroomsByOwnerId_WithAdminRole_ShouldRedirectWithError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(adminId.toString());
        when(authService.findById(adminId)).thenReturn(adminUser);

        String result = chatroomController.getChatroomsByOwnerId(ownerId, session, model, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
    }

    @Test
    void viewChatroomDetail_WithValidRenter_ShouldReturnChatroomView() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(chatroom);
        when(authService.findById(ownerId)).thenReturn(ownerUser);
        when(kosService.findById(propertyId)).thenReturn(Optional.of(kos));

        String result = chatroomController.viewChatroomDetail(chatroomId, session, model, redirectAttributes);

        assertEquals("chat/Chatroom", result);
        verify(model).addAttribute("chatroom", chatroom);
        verify(model).addAttribute("renterName", "renter");
        verify(model).addAttribute("ownerName", "owner");
        verify(model).addAttribute("propertyName", "Test Kos");
        verify(model).addAttribute("user", renterUser);
    }

    @Test
    void viewChatroomDetail_WithValidOwner_ShouldReturnChatroomView() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(ownerId.toString());
        when(authService.findById(ownerId)).thenReturn(ownerUser);
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(chatroom);
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(kosService.findById(propertyId)).thenReturn(Optional.of(kos));

        String result = chatroomController.viewChatroomDetail(chatroomId, session, model, redirectAttributes);

        assertEquals("chat/Chatroom", result);
        verify(model).addAttribute("chatroom", chatroom);
        verify(model).addAttribute("renterName", "renter");
        verify(model).addAttribute("ownerName", "owner");
        verify(model).addAttribute("propertyName", "Test Kos");
        verify(model).addAttribute("user", ownerUser);
    }

    @Test
    void viewChatroomDetail_WithoutLogin_ShouldRedirectToLogin() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        String result = chatroomController.viewChatroomDetail(chatroomId, session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", result);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
    }

    @Test
    void viewChatroomDetail_WithInvalidRole_ShouldRedirectWithError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(adminId.toString());
        when(authService.findById(adminId)).thenReturn(adminUser);

        String result = chatroomController.viewChatroomDetail(chatroomId, session, model, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
    }

    @Test
    void viewChatroomDetail_WithNonExistentChatroom_ShouldRedirectWithError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomById(chatroomId)).thenThrow(new RuntimeException("Chatroom not found"));

        String result = chatroomController.viewChatroomDetail(chatroomId, session, model, redirectAttributes);

        assertEquals("redirect:/chatrooms/renter/" + renterId, result);
        verify(redirectAttributes).addFlashAttribute("error", "Chatroom tidak ditemukan");
    }

    @Test
    void getCurrentUser_WithInvalidToken_ShouldReturnNull() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenThrow(new RuntimeException("Invalid token"));

        String result = chatroomController.getChatroomsByRenterId(renterId, session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", result);
        verify(redirectAttributes).addFlashAttribute("error", "Sesi login Anda telah berakhir. Silakan login kembali.");
        verify(session).removeAttribute("JWT_TOKEN");
    }

    @Test
    void getUserEmailById_WithValidUser_ShouldReturnEmailPrefix() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomsByRenterId(renterId)).thenReturn(Arrays.asList(chatroom));
        when(authService.findById(ownerId)).thenReturn(ownerUser);
        when(kosService.findById(propertyId)).thenReturn(Optional.of(kos));

        chatroomController.getChatroomsByRenterId(renterId, session, model, redirectAttributes);

        verify(authService, atLeastOnce()).findById(ownerId);
    }

    @Test
    void getPropertyNameById_WithValidProperty_ShouldReturnPropertyName() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomsByRenterId(renterId)).thenReturn(Arrays.asList(chatroom));
        when(authService.findById(ownerId)).thenReturn(ownerUser);
        when(kosService.findById(propertyId)).thenReturn(Optional.of(kos));

        chatroomController.getChatroomsByRenterId(renterId, session, model, redirectAttributes);

        verify(kosService).findById(propertyId);
    }

    @Test
    void getPropertyNameById_WithNonExistentProperty_ShouldReturnUnknownProperty() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomsByRenterId(renterId)).thenReturn(Arrays.asList(chatroom));
        when(authService.findById(ownerId)).thenReturn(ownerUser);
        when(kosService.findById(propertyId)).thenReturn(Optional.empty());

        String result = chatroomController.getChatroomsByRenterId(renterId, session, model, redirectAttributes);

        assertEquals("chat/ChatroomList", result);
        verify(kosService).findById(propertyId);
    }

    @Test
    void getUserEmailById_WithServiceException_ShouldReturnUnknownUser() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomsByRenterId(renterId)).thenReturn(Arrays.asList(chatroom));
        when(authService.findById(ownerId)).thenThrow(new RuntimeException("Service error"));
        when(kosService.findById(propertyId)).thenReturn(Optional.of(kos));

        String result = chatroomController.getChatroomsByRenterId(renterId, session, model, redirectAttributes);

        assertEquals("chat/ChatroomList", result);
        verify(authService).findById(ownerId);
    }

    @Test
    void viewChatroomDetail_WithPropertyNotFoundException_ShouldStillWork() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(chatroom);
        when(authService.findById(ownerId)).thenReturn(ownerUser);
        when(kosService.findById(propertyId)).thenThrow(new RuntimeException("Property service error"));

        String result = chatroomController.viewChatroomDetail(chatroomId, session, model, redirectAttributes);

        assertEquals("chat/Chatroom", result);
        verify(model).addAttribute("chatroom", chatroom);
        verify(model).addAttribute("renterName", "renter");
        verify(model).addAttribute("ownerName", "owner");
        verify(model).addAttribute(eq("propertyName"), argThat(name ->
                name.toString().startsWith("Property-") && name.toString().length() > 9));
        verify(model).addAttribute("user", renterUser);
    }

    @Test
    void viewChatroomDetail_WithUserNotFoundException_ShouldStillWork() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(jwtToken);
        when(authService.decodeToken(jwtToken)).thenReturn(renterId.toString());
        when(authService.findById(renterId)).thenReturn(renterUser);
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(chatroom);
        when(authService.findById(ownerId)).thenThrow(new RuntimeException("User service error"));
        when(kosService.findById(propertyId)).thenReturn(Optional.of(kos));

        String result = chatroomController.viewChatroomDetail(chatroomId, session, model, redirectAttributes);

        assertEquals("chat/Chatroom", result);
        verify(model).addAttribute("chatroom", chatroom);
        verify(model).addAttribute("renterName", "renter");
        verify(model).addAttribute("ownerName", "Unknown User");
        verify(model).addAttribute("propertyName", "Test Kos");
        verify(model).addAttribute("user", renterUser);
    }
}