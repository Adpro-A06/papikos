package id.ac.ui.cs.advprog.papikos.wishlist;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.KosType;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WishlistTest {

   
    @Configuration
    static class TestConfig {
        @Bean
        public wishlistService wishlistService() {
           
            return new wishlistService(WishlistNotifier.getInstance());
        }
    }

    
    @Autowired
    private wishlistService wishlistService;

    private Wishlist wishlist;
    private Kos kosA;
    private Kos kosB;

    @BeforeEach
    void setUp() {
        wishlist = new Wishlist("Test Wishlist");
        kosA = new Kos("K001", "Kos GojoMyLove", KosType.PUTRA);
        kosB = new Kos("K002", "Kos SatoruMyShayla", KosType.PUTRI);
    }

    @Test
    void testCreateEmptyWishlist() {
        wishlist.setName("Empty Wishlist");
        Wishlist actual = wishlistService.createWishlist(wishlist);
        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(0, actual.getKosList().size());
    }

    @Test
    void testCreateWishlistWithOneKos() {
        wishlist.setName("Satu Kos");
        wishlist.addKos(kosA);
        Wishlist actual = wishlistService.createWishlist(wishlist);
        assertNotNull(actual.getId());
        assertEquals(1, actual.getKosList().size());
        assertTrue(actual.getKosList().contains(kosA));
    }

    @Test
    void testCreateWishlistWithMultipleKos() {
        wishlist.setName("Beberapa Kos");
        wishlist.addKos(kosA);
        wishlist.addKos(kosB);
        Wishlist actual = wishlistService.createWishlist(wishlist);
        assertNotNull(actual.getId());
        assertEquals(2, actual.getKosList().size());
    }

    @Test
    void testAddDuplicateKos() {
        wishlist.setName("Duplikat Kos");
        wishlist.addKos(kosA);
        wishlist.addKos(kosA);
        Wishlist actual = wishlistService.createWishlist(wishlist);
        
        assertEquals(1, actual.getKosList().size());
    }

    @Test
    void testCreateWishlistWithInvalidName() {
        wishlist.setName("");
        Wishlist actual = wishlistService.createWishlist(wishlist);
        
        assertNull(actual);
    }
}
