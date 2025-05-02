package id.ac.ui.cs.advprog.papikos.wishlist.repository;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WishlistRepositoryTest {

    private WishlistRepository wishlistRepository;

    @BeforeEach
    void setUp() {
        wishlistRepository = new WishlistRepository();
    }

    @Test
    void testSaveWishlist() {
        Wishlist wishlist = new Wishlist("My Wishlist", "user1");
        Wishlist saved = wishlistRepository.save(wishlist);

        assertNotNull(saved);
        assertEquals("My Wishlist", saved.getName());
    }

    @Test
    void testFindAllReturnsAllWishlists() {
        Wishlist w1 = new Wishlist("List 1", "user1");
        Wishlist w2 = new Wishlist("List 2", "user2");

        wishlistRepository.save(w1);
        wishlistRepository.save(w2);

        List<Wishlist> all = wishlistRepository.findAll();

        assertEquals(2, all.size());
        assertTrue(all.contains(w1));
        assertTrue(all.contains(w2));
    }

    @Test
    void testFindByIdReturnsCorrectWishlist() {
        Wishlist wishlist = new Wishlist("Target Wishlist", "user1");
        wishlist.setId(1);

        wishlistRepository.save(wishlist);

        Wishlist found = wishlistRepository.findById(1);

        assertNotNull(found);
        assertEquals("Target Wishlist", found.getName());
    }

    @Test
    void testDeleteWishlist() {
        Wishlist wishlist = new Wishlist("To Delete", "user1");
        wishlist.setId(2);

        wishlistRepository.save(wishlist);
        wishlistRepository.deleteById(2);

        Wishlist deleted = wishlistRepository.findById(2);
        assertNull(deleted);
    }
}
