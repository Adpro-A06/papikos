package id.ac.ui.cs.advprog.papikos.wishlist.model;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WishlistTest {

    private Wishlist wishlist;
    private List<Kos> kosList;

    @BeforeEach
    void setUp() {
        kosList = new ArrayList<>();
        Kos kos = new Kos();
        kos.setId(UUID.randomUUID());
        kosList.add(kos);
    }

    @Test
    void testWishlistConstructor_WithNameAndUserId() {
        wishlist = new Wishlist("Test Wishlist", "user123");
        
        assertEquals("Test Wishlist", wishlist.getName());
        assertEquals("user123", wishlist.getUserId());
        assertNotNull(wishlist.getKosList());
    }

    @Test
    void testWishlistConstructor_WithNameOnly() {
        wishlist = new Wishlist("Test Wishlist");
        
        assertEquals("Test Wishlist", wishlist.getName());
        assertNotNull(wishlist.getKosList());
    }

    @Test
    void testWishlistConstructor_Default() {
        wishlist = new Wishlist();
        
        assertNotNull(wishlist.getKosList());
    }

    @Test
    void testSettersAndGetters() {
        wishlist = new Wishlist();
        
        wishlist.setId(1);
        wishlist.setName("Updated Name");
        wishlist.setUserId("user456");
        wishlist.setKosList(kosList);
        wishlist.setInterestedUsers(new ArrayList<>());
        
        assertEquals(1, wishlist.getId());
        assertEquals("Updated Name", wishlist.getName());
        assertEquals("user456", wishlist.getUserId());
        assertEquals(kosList, wishlist.getKosList());
        assertNotNull(wishlist.getInterestedUsers());
    }

    @Test
    void testAddKosToList() {
        wishlist = new Wishlist("Test", "user123");
        Kos newKos = new Kos();
        newKos.setId(UUID.randomUUID());
        
        wishlist.getKosList().add(newKos);
        
        assertEquals(1, wishlist.getKosList().size());
        assertEquals(newKos, wishlist.getKosList().get(0));
    }

    @Test
    void testClearKosList() {
        wishlist = new Wishlist("Test", "user123");
        wishlist.setKosList(kosList);
        
        wishlist.getKosList().clear();
        
        assertTrue(wishlist.getKosList().isEmpty());
    }

    @Test
    void testNullKosList() {
        wishlist = new Wishlist("Test", "user123");
        
        wishlist.setKosList(null);
        
        assertNull(wishlist.getKosList());
    }

    @Test
    void testEmptyConstructorValues() {
        wishlist = new Wishlist();
        
        assertNull(wishlist.getId());
        assertNull(wishlist.getName());
        assertNull(wishlist.getUserId());
        assertNotNull(wishlist.getKosList());
        assertNotNull(wishlist.getInterestedUsers());
    }
}