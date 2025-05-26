package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistSubject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@WebMvcTest({ WishlistController.class, WishlistRestController.class })
public class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private WishlistService wishlistService;

    @SuppressWarnings("removal")
    @MockBean
    private AuthService authService;

    @SuppressWarnings("removal")
    @MockBean
    private WishlistSubject wishlistSubject;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Wishlist validWishlist;
    private User mockUser;
    private UUID userId;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getEmail()).thenReturn("test@example.com");

        validWishlist = new Wishlist("My Test Wishlist", userId.toString());
        validWishlist.setId(1);
    }

    @Test
    public void testWishlistPage_WithValidUser() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");

        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.getUserWishlist(userId)).thenReturn(Arrays.asList(validWishlist));
        when(wishlistService.getWishlistCount(userId)).thenReturn(1);
        when(wishlistService.getUserWishlistKosIdsAsLong(userId)).thenReturn(Arrays.asList(1L, 2L));

        mockMvc.perform(get("/wishlist").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlist/wishlistpage"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("wishlists"))
                .andExpect(model().attributeExists("wishlistCount"))
                .andExpect(model().attributeExists("kosIds"))
                .andExpect(model().attributeExists("hasWishlists"))
                .andExpect(model().attribute("wishlistCount", 1))
                .andExpect(model().attribute("hasWishlists", true));
    }

    @Test
    public void testWishlistPage_WithoutAuth() throws Exception {
        mockMvc.perform(get("/wishlist"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"));
    }

    @Test
    public void testWishlistPage_EmptyWishlist() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");

        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.getUserWishlist(userId)).thenReturn(new ArrayList<>());
        when(wishlistService.getWishlistCount(userId)).thenReturn(0);
        when(wishlistService.getUserWishlistKosIdsAsLong(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/wishlist").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlist/wishlistpage"))
                .andExpect(model().attribute("wishlistCount", 0))
                .andExpect(model().attribute("hasWishlists", false));
    }

    @Test
    public void testWishlistPageAlternative() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");

        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.getUserWishlist(userId)).thenReturn(new ArrayList<>());
        when(wishlistService.getWishlistCount(userId)).thenReturn(0);
        when(wishlistService.getUserWishlistKosIdsAsLong(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/wishlist/page").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlist/wishlistpage"));
    }

    @Test
    public void testWishlistPage_InvalidToken() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "invalid-token");

        when(authService.decodeToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/wishlist").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"));
    }

    @Test
    public void testAddToWishlist_Valid() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.isInWishlist(userId, 1L)).thenReturn(false);
        when(wishlistService.addToWishlist(any(Wishlist.class))).thenReturn(validWishlist);
        when(wishlistService.getWishlistCount(userId)).thenReturn(1);

        String requestBody = "{\"kosId\": 1}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Berhasil ditambahkan ke wishlist"))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.kosId").value(1))
                .andExpect(jsonPath("$.data.added").value(true));

        verify(wishlistSubject).notifyObservers(any(Wishlist.class), eq("added"));
    }

    @Test
    public void testAddToWishlist_AlreadyExists() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.isInWishlist(userId, 1L)).thenReturn(true);

        String requestBody = "{\"kosId\": 1}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kos sudah ada di wishlist"));
    }

    @Test
    public void testAddToWishlist_InvalidKosId() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);

        String requestBody = "{\"kosId\": -1}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId must be a positive number"));
    }

    @Test
    public void testAddToWishlist_MissingKosId() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);

        String requestBody = "{}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kosId is required"));
    }

    @Test
    public void testAddToWishlist_Unauthorized() throws Exception {
        when(authService.decodeToken("invalid-token")).thenReturn(null);

        String requestBody = "{\"kosId\": 1}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));
    }

    @Test
    public void testRemoveFromWishlist_Valid() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.removeFromWishlist(userId, 1L)).thenReturn(true);
        when(wishlistService.getWishlistCount(userId)).thenReturn(0);

        String requestBody = "{\"kosId\": 1}";

        mockMvc.perform(post("/api/v1/wishlist/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Berhasil dihapus dari wishlist"))
                .andExpect(jsonPath("$.data.removed").value(true));

        verify(wishlistSubject).notifyObservers(any(Wishlist.class), eq("removed"));
    }

    @Test
    public void testToggleWishlist_AddItem() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.isInWishlist(userId, 1L)).thenReturn(false);
        when(wishlistService.addToWishlist(any(Wishlist.class))).thenReturn(validWishlist);
        when(wishlistService.getWishlistCount(userId)).thenReturn(1);

        String requestBody = "{\"kosId\": 1}";

        mockMvc.perform(post("/api/v1/wishlist/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.added").value(true))
                .andExpect(jsonPath("$.data.action").value("added"));
    }

    @Test
    public void testGetUserWishlist() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.getUserWishlist(userId)).thenReturn(Arrays.asList(validWishlist));

        mockMvc.perform(get("/api/v1/wishlist/user")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Wishlist berhasil diambil"))
                .andExpect(jsonPath("$.data.wishlistItems").isArray())
                .andExpect(jsonPath("$.data.wishlistCount").value(1));
    }

    @Test
    public void testGetUserWishlistIds() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.getUserWishlistKosIdsAsLong(userId)).thenReturn(Arrays.asList(1L, 2L));

        mockMvc.perform(get("/api/v1/wishlist/user-ids")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Wishlist IDs berhasil diambil"))
                .andExpect(jsonPath("$.data.kosIds").isArray())
                .andExpect(jsonPath("$.data.kosIds[0]").value(1))
                .andExpect(jsonPath("$.data.kosIds[1]").value(2));
    }

    @Test
    public void testServiceException() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.isInWishlist(userId, 1L)).thenThrow(new RuntimeException("Database error"));

        String requestBody = "{\"kosId\": 1}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testCreateWishlistWithKos_ValidInput() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.isInWishlist(userId, 123L)).thenReturn(false);
        when(wishlistService.addToWishlist(any(Wishlist.class))).thenReturn(validWishlist);
        when(wishlistService.getWishlistCount(userId)).thenReturn(1);

        String requestBody = "{\"kosId\": 123}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(wishlistService).addToWishlist(any(Wishlist.class));
    }

    @Test
    public void testParseKosId_ValidIntegerInput() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.isInWishlist(userId, 123L)).thenReturn(false);
        when(wishlistService.addToWishlist(any(Wishlist.class))).thenReturn(validWishlist);

        String requestBody = "{\"kosId\": 123}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    public void testParseKosId_ValidStringInput() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.isInWishlist(userId, 456L)).thenReturn(false);
        when(wishlistService.addToWishlist(any(Wishlist.class))).thenReturn(validWishlist);

        String requestBody = "{\"kosId\": \"456\"}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    public void testParseKosId_InvalidStringInput() throws Exception {
        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);

        String requestBody = "{\"kosId\": \"invalid\"}";

        mockMvc.perform(post("/api/v1/wishlist/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testExtractTokenFromHeader_ValidHeader() throws Exception {
        when(authService.decodeToken("valid-token-here")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.getUserWishlist(userId)).thenReturn(Arrays.asList(validWishlist));

        mockMvc.perform(get("/api/v1/wishlist/user")
                .header("Authorization", "Bearer valid-token-here"))
                .andExpect(status().isOk());

        verify(authService).decodeToken("valid-token-here");
    }

    @Test
    public void testExtractTokenFromHeader_InvalidHeader() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist/user")
                .header("Authorization", "Invalid header"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token tidak valid"));
    }

    @Test
    public void testExtractTokenFromHeader_MissingHeader() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist/user"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testWishlistController_ErrorHandling() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");

        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        when(wishlistService.getUserWishlist(userId)).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/wishlist").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @Test
    public void testWishlistController_SessionEdgeCases() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "");

        mockMvc.perform(get("/wishlist").session(session))
                .andExpect(status().is3xxRedirection());

        MockHttpSession session2 = new MockHttpSession();
        session2.setAttribute("userId", userId.toString());

        mockMvc.perform(get("/wishlist").session(session2))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testWishlistController_AuthServiceNullReturn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");

        when(authService.decodeToken("valid-token")).thenReturn("");

        mockMvc.perform(get("/wishlist").session(session))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testToggleWishlistLoggedIn() throws Exception {
        UUID kosId = UUID.randomUUID();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");

        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        doNothing().when(wishlistService).toggleWishlist(userId, kosId);

        mockMvc.perform(get("/wishlist/toggle/{kosId}", kosId.toString())
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/penyewa/home"));

        verify(wishlistService).toggleWishlist(userId, kosId);
    }

    @Test
    public void testToggleWishlistNotLoggedIn() throws Exception {
        UUID kosId = UUID.randomUUID();
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/wishlist/toggle/{kosId}", kosId.toString())
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));

        verify(wishlistService, never()).toggleWishlist(any(), any());
    }

    @Test
    public void testClearWishlistLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");

        when(authService.decodeToken("valid-token")).thenReturn(userId.toString());
        when(authService.findById(userId)).thenReturn(mockUser);
        doNothing().when(wishlistService).clearUserWishlist(userId);

        mockMvc.perform(post("/wishlist/clear")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist"))
                .andExpect(flash().attribute("success", "Wishlist berhasil dikosongkan."));

        verify(wishlistService).clearUserWishlist(userId);
    }

    @Test
    public void testClearWishlistNotLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/wishlist/clear")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"))
                .andExpect(flash().attributeExists("error"));

        verify(wishlistService, never()).clearUserWishlist(any());
    }

    @Test
    public void testToggleWishlistInvalidToken() throws Exception {
        UUID kosId = UUID.randomUUID();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "invalid-token");

        when(authService.decodeToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/wishlist/toggle/{kosId}", kosId.toString())
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"));

        verify(wishlistService, never()).toggleWishlist(any(), any());
    }

    @Test
    public void testClearWishlistInvalidToken() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "invalid-token");

        when(authService.decodeToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(post("/wishlist/clear")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/auth/login"));

        verify(wishlistService, never()).clearUserWishlist(any());
    }
}