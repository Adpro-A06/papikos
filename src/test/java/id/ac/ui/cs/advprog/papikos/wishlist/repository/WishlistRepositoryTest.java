package id.ac.ui.cs.advprog.papikos.wishlist.repository;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WishlistRepositoryTest {

    private WishlistRepository repository;
    private Wishlist testWishlist;

    @BeforeEach
    void setUp() {
        repository = new WishlistRepository();
        testWishlist = new Wishlist("Test Wishlist", "user123");
    }

    @Test
    void testSave() {
        Wishlist saved = repository.save(testWishlist);
        
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(testWishlist.getName(), saved.getName());
    }

    @Test
    void testFindAll() {
        repository.save(testWishlist);
        
        List<Wishlist> all = repository.findAll();
        
        assertEquals(1, all.size());
        assertEquals(testWishlist.getName(), all.get(0).getName());
    }

    @Test
    void testFindById_Exists() {
        Wishlist saved = repository.save(testWishlist);
        
        Wishlist found = repository.findById(saved.getId());
        
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void testFindById_NotExists() {
        Wishlist found = repository.findById(999);
        
        assertNull(found);
    }

    @Test
    void testDeleteById_Exists() {
        Wishlist saved = repository.save(testWishlist);
        
        boolean deleted = repository.deleteById(saved.getId());
        
        assertTrue(deleted);
        assertNull(repository.findById(saved.getId()));
    }

    @Test
    void testDeleteById_NotExists() {
        boolean deleted = repository.deleteById(999);
        
        assertFalse(deleted);
    }

    @Test
    void testSaveMultiple() {
        Wishlist wishlist1 = new Wishlist("Wishlist 1", "user1");
        Wishlist wishlist2 = new Wishlist("Wishlist 2", "user2");
        
        repository.save(wishlist1);
        repository.save(wishlist2);
        
        List<Wishlist> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testAutoIncrement() {
        Wishlist wishlist1 = repository.save(new Wishlist("W1", "u1"));
        Wishlist wishlist2 = repository.save(new Wishlist("W2", "u2"));
        
        assertNotEquals(wishlist1.getId(), wishlist2.getId());
        assertTrue(wishlist2.getId() > wishlist1.getId());
    }

    @Test
    void testSaveNullWishlist() {
        assertThrows(Exception.class, () -> {
            repository.save(null);
        });
    }
}