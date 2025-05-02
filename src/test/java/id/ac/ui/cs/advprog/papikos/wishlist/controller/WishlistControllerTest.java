package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WishlistController.class)
public class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private WishlistService wishlistService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Wishlist validWishlist;

    @BeforeEach
    public void setUp() {
        validWishlist = new Wishlist("My Test Wishlist");
        validWishlist.setId(1);
    }

    @Test
    public void testCreateWishlist_InvalidName() throws Exception {
        Wishlist wishlist = new Wishlist("");
        when(wishlistService.createWishlist(any(Wishlist.class))).thenReturn(null);
        mockMvc.perform(post("/api/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wishlist)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWishlist_Valid() throws Exception {
        when(wishlistService.createWishlist(any(Wishlist.class))).thenReturn(validWishlist);
        mockMvc.perform(post("/api/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validWishlist)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Test Wishlist"));
    }
}
