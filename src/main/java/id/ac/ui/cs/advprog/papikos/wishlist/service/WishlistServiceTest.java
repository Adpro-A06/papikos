package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WishlistServiceTest {

    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        wishlistService = new WishlistService(new WishlistRepository());
    }

    @Test
    void testCreateWishlist() {
        Wishlist wishlist = wishlistService.createWishlist("My Wishlist", "user1");

        assertNotNull(wishlist);
        assertEquals("My Wishlist", wishlist.getName());
        assertEquals("user1", wishlist.getUserId());
    }

    @Test
    void testGetAllWishlists() {
        wishlistService.createWishlist("List 1", "user1");
        wishlistService.createWishlist("List 2", "user2");

        List<Wishlist> allWishlists = wishlistService.getAllWishlists();

        assertEquals(2, allWishlists.size());
    }

    @Test
    void testGetWishlistById() {
        Wishlist wishlist = wishlistService.createWishlist("Target List", "userX");
        int id = wishlist.getId();

        Wishlist found = wishlistService.getWishlistById(id);

        assertNotNull(found);
        assertEquals("Target List", found.getName());
    }

    @Test
    void testDeleteWishlistById() {
        Wishlist wishlist = wishlistService.createWishlist("To Delete", "userX");
        int id = wishlist.getId();

        boolean deleted = wishlistService.deleteWishlistById(id);
        assertTrue(deleted);

        assertNull(wishlistService.getWishlistById(id));
    }

    @Test
    void testWishlistExists() {
        Wishlist wishlist = wishlistService.createWishlist("Exists", "user1");
        wishlist.setKosId("kos123");

        assertTrue(wishlistService.wishlistExists("user1", "kos123"));
        assertFalse(wishlistService.wishlistExists("user1", "kosXYZ"));
    }
}
