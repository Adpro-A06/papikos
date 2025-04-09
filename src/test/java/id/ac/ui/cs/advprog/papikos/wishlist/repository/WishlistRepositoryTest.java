package id.ac.ui.cs.advprog.papikos.wishlist.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;


public class WishlistRepositoryTest {

    private WishlistRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new WishlistRepository();
    }

    @Test
    public void testSaveWishlist() {
        
        Wishlist wishlist = new Wishlist("user1");
        Wishlist savedWishlist = repository.save(wishlist);
        
        assertNotNull(savedWishlist, "Saved wishlist should not be null");
        assertEquals(wishlist, savedWishlist, "Saved wishlist should equal the original wishlist");
    }

    @Test
    public void testFindAllEmpty() {
       
        List<Wishlist> all = repository.findAll();
        
        assertNotNull(all, "findAll should not return null");
        assertTrue(all.isEmpty(), "findAll should return an empty list when no wishlist exists");
    }

    

    @Test
    public void testDeleteWishlist() {
      
        Wishlist wishlist = new Wishlist();
        repository.save(wishlist);
        repository.delete(wishlist);
        List<Wishlist> all = repository.findAll();
        
        assertFalse(all.contains(wishlist), "Wishlist should not be present after deletion");
    }

    @Test
    public void testFindByUserId() {
        Wishlist wishlist1 = new Wishlist("Kos A");
        wishlist1.setUserId("user1");

        Wishlist wishlist2 = new Wishlist("Kos B");
        wishlist2.setUserId("user1");

        Wishlist wishlist3 = new Wishlist("Kos C");
        wishlist3.setUserId("user2");

        repository.save(wishlist1);
        repository.save(wishlist2);
        repository.save(wishlist3);

        List<Wishlist> userList = repository.findByUserId("user1");

        assertEquals(2, userList.size(), "User1 should have 2 wishlists");
    }

    @Test
    public void testFindByUserIdAndKosId() {
        Wishlist wishlist = new Wishlist("Kos X");
        wishlist.setUserId("user1");
        wishlist.setKosId("kos1");

        repository.save(wishlist);

        Optional<Wishlist> result = repository.findByUserIdAndKosId("user1", "kos1");

        assertTrue(result.isPresent(), "Wishlist should be found for the given userId and kosId");
        assertEquals(wishlist, result.get(), "Found wishlist should match the saved wishlist");
    }


}
