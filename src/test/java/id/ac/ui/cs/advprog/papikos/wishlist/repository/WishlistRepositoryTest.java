package id.ac.ui.cs.advprog.papikos.wishlist.repository;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

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
        
        Optional<Wishlist> found = repository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void testFindById_NotExists() {
        Optional<Wishlist> found = repository.findById(999); 
        
        assertFalse(found.isPresent()); 
    }

    @Test
    void testDeleteById_Exists() {
        Wishlist saved = repository.save(testWishlist);
        
        boolean deleted = repository.deleteById(saved.getId());
        
        assertTrue(deleted);
        assertFalse(repository.findById(saved.getId()).isPresent()); 
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
    void testFindById_EmptyOptional() {
        Optional<Wishlist> notFound = repository.findById(999);
        
        assertTrue(notFound.isEmpty());
    }

    @Test
    void testFindById_ValidOptional() {
        Wishlist saved = repository.save(testWishlist);
        
        Optional<Wishlist> found = repository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getName(), found.get().getName());
    }

    // Additional test methods to boost coverage
    @Test
    void testRepository_MultipleOperations() {
        // Save multiple
        Wishlist w1 = repository.save(new Wishlist("W1", "u1"));
        Wishlist w2 = repository.save(new Wishlist("W2", "u2"));
        Wishlist w3 = repository.save(new Wishlist("W3", "u3"));
        
        assertEquals(3, repository.findAll().size());
        
        // Delete one
        repository.deleteById(w2.getId());
        assertEquals(2, repository.findAll().size());
        
        // Check remaining
        assertTrue(repository.findById(w1.getId()).isPresent());
        assertFalse(repository.findById(w2.getId()).isPresent());
        assertTrue(repository.findById(w3.getId()).isPresent());
    }
}