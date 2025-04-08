package id.ac.ui.cs.advprog.papikos.wishlist;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WishlistTest {

    private wishlistService wishlistService;
    private Wishlist wishlist;
    private Kos kosA;
    private Kos kosB;

    @BeforeEach
    void setUp() {
        wishlistService = new wishlistService();
        wishlist = new Wishlist();
        kosA = new Kos("K001", "Kos GojoMyLove");
        kosB = new Kos("K002", "Kos SatoruMyShayla");
    }

    
    @Test
    void testCreateEmptyWishlist() {
        wishlist.setName("Empty Wishlist");

        Wishlist actual = wishlistService.createWishlist(wishlist);

        assertNotNull(actual, "Wishlist object seharusnya tidak null");
        assertNotNull(actual.getId(), "Wishlist ID seharusnya terisi");
     
        assertEquals(0, actual.getKosList().size(), "Wishlist seharusnya kosong");
    }

    
    @Test
    void testCreateWishlistWithOneKos() {
        wishlist.setName("Satu Kos");
        wishlist.addKos(kosA);

        Wishlist actual = wishlistService.createWishlist(wishlist);

        assertNotNull(actual.getId(), "Wishlist harus memiliki ID");
        assertEquals(1, actual.getKosList().size(), "Wishlist harus berisi tepat 1 kos");
        assertTrue(actual.getKosList().contains(kosA), "Wishlist harus mengandung kosA");
    }

    
    @Test
    void testCreateWishlistWithMultipleKos() {
        wishlist.setName("Beberapa Kos");
        wishlist.addKos(kosA);
        wishlist.addKos(kosB);

        Wishlist actual = wishlistService.createWishlist(wishlist);

        assertNotNull(actual.getId(), "Wishlist harus memiliki ID");
        assertEquals(2, actual.getKosList().size(), "Wishlist harus berisi 2 kos");
    }

    
    @Test
    void testAddDuplicateKos() {
        wishlist.setName("Duplikat Kos");
        wishlist.addKos(kosA);
        wishlist.addKos(kosA); // duplicate

        Wishlist actual = wishlistService.createWishlist(wishlist);

        assertEquals(1, actual.getKosList().size(), "Wishlist harus hanya menyimpan 1 instance untuk kos yang sama");
    }

    
    @Test
    void testCreateWishlistWithInvalidName() {
        wishlist.setName("");  // nama tidak valid

        Wishlist actual = wishlistService.createWishlist(wishlist);

        
        assertNull(actual, "Wishlist tidak boleh dibuat jika nama tidak valid");
    }
}
