package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
public class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @Autowired
    private ObjectMapper objectMapper;

    private Wishlist sampleWishlist;

    @BeforeEach
    void setUp() {
        sampleWishlist = new Wishlist("My Wishlist", "user1");
        sampleWishlist.setId(1);
        sampleWishlist.setKosList(Collections.singletonList(new Kos()));
    }

    @Test
    void testCreateWishlist() throws Exception {
        when(wishlistService.createWishlist(any(Wishlist.class)))
                .thenReturn(sampleWishlist);

        mockMvc.perform(post("/wishlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleWishlist)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Wishlist"))
                .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    void testGetAllWishlists() throws Exception {
        when(wishlistService.getAllWishlists())
                .thenReturn(Arrays.asList(sampleWishlist));

        mockMvc.perform(get("/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("My Wishlist"));
    }

    @Test
    void testGetWishlistById() throws Exception {
        when(wishlistService.getWishlistById(1))
                .thenReturn(sampleWishlist);

        mockMvc.perform(get("/wishlist/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testDeleteWishlistById() throws Exception {
        when(wishlistService.deleteWishlistById(1))
                .thenReturn(true);

        mockMvc.perform(delete("/wishlist/1"))
                .andExpect(status().isOk());
    }
}
