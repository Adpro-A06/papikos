package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistNotifier mockNotifier;

    @InjectMocks
    private WishlistService wishlistService;

    private UUID userId;
    private Wishlist testWishlist;
    private Kos testKos;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testKos = new Kos();
        testKos.setId(UUID.randomUUID());
        
        testWishlist = new Wishlist("Test Wishlist", userId.toString());
        testWishlist.setId(1);
        List<Kos> kosList = new ArrayList<>();
        kosList.add(testKos);
        testWishlist.setKosList(kosList);
    }

    @Test
    void testCreateWishlist_Valid() {
        Wishlist result = wishlistService.createWishlist(testWishlist);
        
        assertNotNull(result);
        assertEquals(testWishlist.getName(), result.getName());
        verify(mockNotifier).notifyObservers(any(Wishlist.class), eq("created"));
    }

    @Test
    void testCreateWishlist_Invalid() {
        Wishlist invalidWishlist = new Wishlist("", userId.toString());
        
        Wishlist result = wishlistService.createWishlist(invalidWishlist);
        
        assertNull(result);
    }

    @Test
    void testCreateWishlist_NullName() {
        Wishlist invalidWishlist = new Wishlist(null, userId.toString());
        
        Wishlist result = wishlistService.createWishlist(invalidWishlist);
        
        assertNull(result);
    }

    @Test
    void testCreateWishlist_WhitespaceName() {
        Wishlist invalidWishlist = new Wishlist("   ", userId.toString());
        
        Wishlist result = wishlistService.createWishlist(invalidWishlist);
        
        assertNull(result);
    }

    @Test
    void testIsInWishlist_Found() {
        wishlistService.createWishlist(testWishlist);
        Long kosId = Math.abs(testKos.getId().hashCode()) % Long.MAX_VALUE;
        
        boolean result = wishlistService.isInWishlist(userId, kosId);
        
        assertTrue(result);
    }

    @Test
    void testIsInWishlist_NotFound() {
        boolean result = wishlistService.isInWishlist(userId, 999L);
        
        assertFalse(result);
    }

    @Test
    void testAddToWishlist_New() {
        Wishlist result = wishlistService.addToWishlist(testWishlist);
        
        assertNotNull(result);
        verify(mockNotifier).notifyObservers(any(Wishlist.class), eq("added"));
    }

    @Test
    void testAddToWishlist_Duplicate() {
        wishlistService.addToWishlist(testWishlist);
        
        Wishlist duplicate = wishlistService.addToWishlist(testWishlist);
        
        assertNotNull(duplicate);
    }

    @Test
    void testRemoveFromWishlist_Exists() {
        wishlistService.addToWishlist(testWishlist);
        Long kosId = Math.abs(testKos.getId().hashCode()) % Long.MAX_VALUE;
        
        boolean result = wishlistService.removeFromWishlist(userId, kosId);
        
        assertTrue(result);
    }

    @Test
    void testRemoveFromWishlist_NotExists() {
        boolean result = wishlistService.removeFromWishlist(userId, 999L);
        
        assertFalse(result);
    }

    @Test
    void testGetWishlistCount_WithItems() {
        wishlistService.addToWishlist(testWishlist);
        
        int count = wishlistService.getWishlistCount(userId);
        
        assertEquals(1, count);
    }

    @Test
    void testGetWishlistCount_Empty() {
        int count = wishlistService.getWishlistCount(userId);
        
        assertEquals(0, count);
    }

    @Test
    void testClearUserWishlist() {
        wishlistService.addToWishlist(testWishlist);
        
        wishlistService.clearUserWishlist(userId);
        
        assertEquals(0, wishlistService.getWishlistCount(userId));
    }

    @Test
    void testGetUserWishlist_WithItems() {
        wishlistService.addToWishlist(testWishlist);
        
        List<Wishlist> result = wishlistService.getUserWishlist(userId);
        
        assertEquals(1, result.size());
        assertEquals(testWishlist.getName(), result.get(0).getName());
    }

    @Test
    void testGetUserWishlist_Empty() {
        List<Wishlist> result = wishlistService.getUserWishlist(userId);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserWishlistKosIdsAsLong() {
        wishlistService.addToWishlist(testWishlist);
        
        List<Long> result = wishlistService.getUserWishlistKosIdsAsLong(userId);
        
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void testGetUserWishlistKosIdsAsLong_Empty() {
        List<Long> result = wishlistService.getUserWishlistKosIdsAsLong(userId);
        
        assertTrue(result.isEmpty());
    }


    @Test
    void testCreateWishlist_WithNullName() {
        Wishlist wishlist = new Wishlist();
        wishlist.setName(null);
        
        Wishlist result = wishlistService.createWishlist(wishlist);
        
        assertNull(result); // Should return null for invalid wishlist
    }

    @Test
    void testCreateWishlist_WithEmptyName() {
        Wishlist wishlist = new Wishlist();
        wishlist.setName("   "); // Just whitespace
        
        Wishlist result = wishlistService.createWishlist(wishlist);
        
        assertNull(result);
    }

    @Test
    void testFilterDuplicateKos() {
        Wishlist wishlist = new Wishlist("Test", "user1");
        
        Kos kos1 = new Kos();
        kos1.setId(UUID.randomUUID());
        Kos kos2 = new Kos();
        kos2.setId(UUID.randomUUID());
        
        // Add duplicate
        wishlist.setKosList(Arrays.asList(kos1, kos2, kos1));
        
        Wishlist result = wishlistService.createWishlist(wishlist);
        
        assertEquals(2, result.getKosList().size()); // Should filter duplicates
    }

    

    @Test
    void testRemoveFromWishlist_NotFound() {
        UUID userId = UUID.randomUUID();
        Long kosId = 999L;
        
        boolean result = wishlistService.removeFromWishlist(userId, kosId);
        
        assertFalse(result);
    }
}