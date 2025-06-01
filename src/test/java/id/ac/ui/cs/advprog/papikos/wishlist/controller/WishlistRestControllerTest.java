package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistSubject;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ WishlistRestController.class })
public class WishlistRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private AuthService authService;

    @MockBean
    private WishlistSubject wishlistSubject;

    private User testUser;
    private Wishlist testWishlist;
    private UUID userId;
    private String validToken = "valid-token";
    private String validAuthHeader = "Bearer " + validToken;
    private Long validKosId = 123L;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(userId);

        testWishlist = new Wishlist("Test Wishlist", userId.toString());
        testWishlist.setId(1);

        List<Kos> kosList = new ArrayList<>();
        Kos kos = new Kos();
        kos.setId(UUID.randomUUID());
        kosList.add(kos);
        testWishlist.setKosList(kosList);
    }

    @Test
    void addToWishlist_Success() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.isInWishlist(userId, validKosId)).thenReturn(false);
        when(wishlistService.addToWishlist(any())).thenReturn(testWishlist);
        when(wishlistService.getWishlistCount(userId)).thenReturn(1);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Berhasil ditambahkan ke wishlist"))
                .andExpect(jsonPath("$.data.added").value(true))
                .andExpect(jsonPath("$.data.wishlistCount").value(1));

        verify(wishlistSubject).notifyObservers(any(), eq("added"));
    }

    @Test
    void addToWishlist_AlreadyExists() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.isInWishlist(userId, validKosId)).thenReturn(true);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kos sudah ada di wishlist"));

        verify(wishlistSubject, never()).notifyObservers(any(), any());
    }

    @Test
    void addToWishlist_InvalidToken() throws Exception {
        when(authService.decodeToken(anyString())).thenReturn(null);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));
    }

    @Test
    void addToWishlist_MissingKosId() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId is required"));
    }

    @Test
    void addToWishlist_InvalidKosId() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId must be a positive number"));
    }

    @Test
    void addToWishlist_AddFailed() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.isInWishlist(userId, validKosId)).thenReturn(false);
        when(wishlistService.addToWishlist(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Gagal menambahkan ke wishlist"));
    }

    @Test
    void addToWishlist_Exception() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.isInWishlist(userId, validKosId)).thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Gagal menambahkan ke wishlist")));
    }

    @Test
    void removeFromWishlist_Success() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.removeFromWishlist(userId, validKosId)).thenReturn(true);
        when(wishlistService.getWishlistCount(userId)).thenReturn(0);

        mockMvc.perform(post("/api/v1/wishlist/remove")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Berhasil dihapus dari wishlist"))
                .andExpect(jsonPath("$.data.removed").value(true))
                .andExpect(jsonPath("$.data.wishlistCount").value(0));

        verify(wishlistSubject).notifyObservers(any(), eq("removed"));
    }

    @Test
    void removeFromWishlist_NotFound() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.removeFromWishlist(userId, validKosId)).thenReturn(false);

        mockMvc.perform(post("/api/v1/wishlist/remove")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Item tidak ditemukan di wishlist"));

        verify(wishlistSubject, never()).notifyObservers(any(), any());
    }

    @Test
    void removeFromWishlist_InvalidRequest() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/remove")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId is required"));

        mockMvc.perform(post("/api/v1/wishlist/remove")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": \"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId must be a positive number"));
    }

    @Test
    void toggleWishlist_Add() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.isInWishlist(userId, validKosId)).thenReturn(false);
        when(wishlistService.addToWishlist(any())).thenReturn(testWishlist);
        when(wishlistService.getWishlistCount(userId)).thenReturn(1);

        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.added").value(true))
                .andExpect(jsonPath("$.data.action").value("added"))
                .andExpect(jsonPath("$.data.wishlistCount").value(1));

        verify(wishlistSubject).notifyObservers(any(), eq("added"));
    }

    @Test
    void toggleWishlist_Remove() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.isInWishlist(userId, validKosId)).thenReturn(true);
        when(wishlistService.removeFromWishlist(userId, validKosId)).thenReturn(true);
        when(wishlistService.getWishlistCount(userId)).thenReturn(0);

        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.added").value(false))
                .andExpect(jsonPath("$.data.action").value("removed"))
                .andExpect(jsonPath("$.data.wishlistCount").value(0));

        verify(wishlistSubject).notifyObservers(any(), eq("removed"));
    }

    @Test
    void toggleWishlist_InvalidRequest() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId is required"));

        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId must be a positive number"));
    }

    @Test
    void toggleWishlist_Exception() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.isInWishlist(userId, validKosId)).thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Gagal mengupdate wishlist")));
    }

    @Test
    void clearWishlist_Success() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        doNothing().when(wishlistService).clearUserWishlist(userId);

        mockMvc.perform(post("/api/v1/wishlist/clear")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Wishlist berhasil dikosongkan"))
                .andExpect(jsonPath("$.data.wishlistCount").value(0));

        verify(wishlistSubject).notifyObservers(any(), eq("cleared"));
        verify(wishlistService).clearUserWishlist(userId);
    }

    @Test
    void clearWishlist_InvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/wishlist/clear")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));

        verify(wishlistService, never()).clearUserWishlist(any());
    }

    @Test
    void clearWishlist_Exception() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        doThrow(new RuntimeException("Test exception")).when(wishlistService).clearUserWishlist(userId);

        mockMvc.perform(post("/api/v1/wishlist/clear")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Gagal mengosongkan wishlist")));
    }

    @Test
    void getUserWishlist_Success() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.getUserWishlist(userId)).thenReturn(Arrays.asList(testWishlist));

        mockMvc.perform(get("/api/v1/wishlist/user")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Wishlist berhasil diambil"))
                .andExpect(jsonPath("$.data.wishlistItems").isArray())
                .andExpect(jsonPath("$.data.wishlistCount").value(1));
    }

    @Test
    void getUserWishlist_InvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist/user")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));
    }

    @Test
    void getUserWishlist_Exception() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.getUserWishlist(userId)).thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(get("/api/v1/wishlist/user")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Gagal mengambil wishlist")));
    }

    @Test
    void getUserWishlistIds_Success() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.getUserWishlistKosIdsAsLong(userId)).thenReturn(Arrays.asList(validKosId));

        mockMvc.perform(get("/api/v1/wishlist/user-ids")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Wishlist IDs berhasil diambil"))
                .andExpect(jsonPath("$.data.kosIds").isArray())
                .andExpect(jsonPath("$.data.kosIds[0]").value(validKosId));
    }

    @Test
    void getUserWishlistIds_InvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist/user-ids")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));
    }

    @Test
    void getUserWishlistIds_Exception() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.getUserWishlistKosIdsAsLong(userId)).thenThrow(new RuntimeException("Test exception"));

        mockMvc.perform(get("/api/v1/wishlist/user-ids")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Gagal mengambil wishlist IDs")));
    }

    @Test
    void extractTokenFromHeader_InvalidFormat() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist/user")
                .header("Authorization", "Invalid-format"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void extractTokenFromHeader_EmptyToken() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist/user")
                .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void parseKosId_DifferentTypes() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.isInWishlist(eq(userId), anyLong())).thenReturn(false);
        when(wishlistService.addToWishlist(any())).thenReturn(testWishlist);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": \"456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void parseKosId_InvalidString() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": \"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void removeFromWishlist_ServerError() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);
        when(wishlistService.removeFromWishlist(any(), anyLong()))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(post("/api/v1/wishlist/remove")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Gagal menghapus dari wishlist")));
    }

    @Test
    void removeFromWishlist_NullRequest() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/remove")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleWishlist_Unauthorized_EmptyToken() throws Exception {
        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .header("Authorization", "Bearer ")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));
    }

    @Test
    void toggleWishlist_NullKosId() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId must be a positive number"));
    }

    @Test
    void toggleWishlist_DecodeTokenThrowsException() throws Exception {
        when(authService.decodeToken(validToken)).thenThrow(new RuntimeException("Invalid token format"));

        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));
    }

    @Test
    void createWishlistWithKos_EmptyUserId() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(" ");
        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWishlistWithKos_ZeroKosId() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId must be a positive number"));
    }

    @Test
    void parseKosId_NullObject() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"someOtherField\": \"value\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId is required"));
    }

    @Test
    void parseKosId_EmptyString() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId must be a positive number"));
    }

    @Test
    void getCurrentUser_NullUserFromAuthService() throws Exception {
        when(authService.decodeToken(validToken)).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(null);

        mockMvc.perform(post("/api/v1/wishlist/add")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kosId\": " + validKosId + "}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));
    }
}