package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistSubject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.UUID;

@WebMvcTest({WishlistController.class, WishlistRestController.class})
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
        // Setup mock user - create mock instead of setting properties
        userId = UUID.randomUUID();
        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getEmail()).thenReturn("test@example.com");
        
        // Setup valid wishlist
        validWishlist = new Wishlist("My Test Wishlist", userId.toString());
        validWishlist.setId(1);
    }

    // ========== WEB CONTROLLER TESTS ==========
    
    @Test
    public void testWishlistPage_WithValidUser() throws Exception {
        // Setup session with JWT token
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "valid-token");
        
        // Mock service calls
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

    // ========== REST API CONTROLLER TESTS ==========

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
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Gagal menambahkan ke wishlist"));

    

    @Test
    public void testCreateWishlistWithKos_Valid() throws Exception {
        // Test helper method createWishlistWithKos
        String userId = UUID.randomUUID().toString();
        Long kosId = 123L;
        
        // This will test the createWishlistWithKos method
        Wishlist result = createWishlistWithKos(userId, kosId);
        
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getKosList());
        assertEquals(1, result.getKosList().size());
    }

    @Test
    public void testParseKosId_ValidInteger() throws Exception {
        // Test parseKosId method with Integer
        Object kosIdObj = 123;
        Long result = parseKosId(kosIdObj);
        assertEquals(Long.valueOf(123L), result);
    }

    @Test
    public void testParseKosId_ValidString() throws Exception {
        // Test parseKosId method with String
        Object kosIdObj = "456";
        Long result = parseKosId(kosIdObj);
        assertEquals(Long.valueOf(456L), result);
    }

    @Test
    public void testParseKosId_InvalidString() throws Exception {
        // Test parseKosId method with invalid String
        Object kosIdObj = "invalid";
        Long result = parseKosId(kosIdObj);
        assertNull(result);
    }

    @Test
    public void testConvertUUIDToLong_ValidUUID() throws Exception {
        UUID testUuid = UUID.randomUUID();
        Long result = convertUUIDToLong(testUuid);
        assertNotNull(result);
        assertTrue(result > 0);
    }

    @Test
    public void testConvertUUIDToLong_NullUUID() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            convertUUIDToLong(null);
        });
    }

    @Test
    public void testExtractTokenFromHeader_ValidHeader() throws Exception {
        String authHeader = "Bearer valid-token-here";
        String result = extractTokenFromHeader(authHeader);
        assertEquals("valid-token-here", result);
    }

    @Test
    public void testExtractTokenFromHeader_InvalidHeader() throws Exception {
        String authHeader = "Invalid header";
        String result = extractTokenFromHeader(authHeader);
        assertNull(result);
    }

    @Test
    public void testExtractTokenFromHeader_NullHeader() throws Exception {
        String result = extractTokenFromHeader(null);
        assertNull(result);
}
    }
}