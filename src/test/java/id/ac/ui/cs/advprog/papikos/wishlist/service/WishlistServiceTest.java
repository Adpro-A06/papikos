package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistServiceTest {

    private WishlistService wishlistService;

    @Mock
    private WishlistNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        wishlistService = new WishlistService(notifier);
    }

    @Test
    void testCreateWishlist_ValidWishlist() {
        Wishlist wishlist = new Wishlist("Valid Wishlist", "user1");
        wishlist.setKosList(new ArrayList<>());

        Wishlist result = wishlistService.createWishlist(wishlist);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(notifier).notifyObservers(wishlist, "created");
    }

    @Test
    void testCreateWishlist_NullName() {
        Wishlist wishlist = new Wishlist();
        wishlist.setName(null);

        Wishlist result = wishlistService.createWishlist(wishlist);

        assertNull(result);
    }

    @Test
    void testCreateWishlist_EmptyName() {
        Wishlist wishlist = new Wishlist();
        wishlist.setName("   ");

        Wishlist result = wishlistService.createWishlist(wishlist);

        assertNull(result);
    }

    @Test
    void testCreateWishlist_NullWishlist() {
        Wishlist result = wishlistService.createWishlist(null);

        assertNull(result);
    }

    @Test
    void testCreateWishlist_NullKosList() {
        Wishlist wishlist = new Wishlist("Test", "user1");
        wishlist.setKosList(null);

        Wishlist result = wishlistService.createWishlist(wishlist);

        assertNotNull(result);
        assertNotNull(result.getKosList());
        assertEquals(0, result.getKosList().size());
    }

    @Test
    void testIsInWishlist_Found() {
        UUID userId = UUID.randomUUID();
        Kos kos = new Kos();
        kos.setId(UUID.randomUUID());
        
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(Arrays.asList(kos));
        wishlistService.createWishlist(wishlist);

        Long kosId = Math.abs(kos.getId().hashCode()) % Long.MAX_VALUE;
        boolean result = wishlistService.isInWishlist(userId, kosId);

        assertTrue(result);
    }

    @Test
    void testIsInWishlist_NotFound() {
        UUID userId = UUID.randomUUID();
        Long kosId = 999L;

        boolean result = wishlistService.isInWishlist(userId, kosId);

        assertFalse(result);
    }

    @Test
    void testIsInWishlist_NullKosId() {
        UUID userId = UUID.randomUUID();
        Kos kos = new Kos();
        kos.setId(null);
        
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(Arrays.asList(kos));
        wishlistService.createWishlist(wishlist);

        boolean result = wishlistService.isInWishlist(userId, 123L);

        assertFalse(result);
    }

    @Test
    void testAddToWishlist_NewWishlist() {
        Wishlist wishlist = new Wishlist("New", "user1");
        wishlist.setKosList(new ArrayList<>());

        Wishlist result = wishlistService.addToWishlist(wishlist);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(notifier).notifyObservers(wishlist, "added");
    }

    @Test
    void testAddToWishlist_ExistingKos() {
        Kos kos = new Kos();
        kos.setId(UUID.randomUUID());

        Wishlist existing = new Wishlist("Existing", "user1");
        existing.setKosList(Arrays.asList(kos));
        wishlistService.addToWishlist(existing);

        Wishlist duplicate = new Wishlist("Duplicate", "user1");
        duplicate.setKosList(Arrays.asList(kos));
        
        Wishlist result = wishlistService.addToWishlist(duplicate);

        assertNotNull(result);
        // Should not add duplicate - verify only one notification
        verify(notifier, times(1)).notifyObservers(any(), eq("added"));
    }

    @Test
    void testAddToWishlist_NullKosList() {
        Wishlist wishlist = new Wishlist("Test", "user1");
        wishlist.setKosList(null);

        Wishlist result = wishlistService.addToWishlist(wishlist);

        assertNotNull(result);
        assertNotNull(result.getKosList());
    }

    @Test
    void testRemoveFromWishlist_Success() {
        UUID userId = UUID.randomUUID();
        Kos kos = new Kos();
        kos.setId(UUID.randomUUID());
        
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(Arrays.asList(kos));
        wishlistService.createWishlist(wishlist);

        Long kosId = Math.abs(kos.getId().hashCode()) % Long.MAX_VALUE;
        boolean result = wishlistService.removeFromWishlist(userId, kosId);

        assertTrue(result);
    }

    @Test
    void testRemoveFromWishlist_NotFound() {
        UUID userId = UUID.randomUUID();

        boolean result = wishlistService.removeFromWishlist(userId, 999L);

        assertFalse(result);
    }

    @Test
    void testRemoveFromWishlist_NullKosId() {
        UUID userId = UUID.randomUUID();
        Kos kos = new Kos();
        kos.setId(null);
        
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(Arrays.asList(kos));
        wishlistService.createWishlist(wishlist);

        boolean result = wishlistService.removeFromWishlist(userId, 123L);

        assertFalse(result);
    }

    @Test
    void testGetWishlistCount_WithItems() {
        UUID userId = UUID.randomUUID();
        Kos kos1 = new Kos();
        kos1.setId(UUID.randomUUID());
        Kos kos2 = new Kos();
        kos2.setId(UUID.randomUUID());
        
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(Arrays.asList(kos1, kos2));
        wishlistService.createWishlist(wishlist);

        int count = wishlistService.getWishlistCount(userId);

        assertEquals(2, count);
    }

    @Test
    void testGetWishlistCount_NoItems() {
        UUID userId = UUID.randomUUID();

        int count = wishlistService.getWishlistCount(userId);

        assertEquals(0, count);
    }

    @Test
    void testClearUserWishlist() {
        UUID userId = UUID.randomUUID();
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(new ArrayList<>());
        wishlistService.createWishlist(wishlist);

        wishlistService.clearUserWishlist(userId);

        List<Wishlist> result = wishlistService.getUserWishlist(userId);
        assertEquals(0, result.size());
    }

    @Test
    void testGetUserWishlist_WithItems() {
        UUID userId = UUID.randomUUID();
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(new ArrayList<>());
        wishlistService.createWishlist(wishlist);

        List<Wishlist> result = wishlistService.getUserWishlist(userId);

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getName());
    }

    @Test
    void testGetUserWishlist_NoItems() {
        UUID userId = UUID.randomUUID();

        List<Wishlist> result = wishlistService.getUserWishlist(userId);

        assertEquals(0, result.size());
    }

    @Test
    void testGetUserWishlistKosIdsAsLong_WithItems() {
        UUID userId = UUID.randomUUID();
        Kos kos = new Kos();
        kos.setId(UUID.randomUUID());
        
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(Arrays.asList(kos));
        wishlistService.createWishlist(wishlist);

        List<Long> result = wishlistService.getUserWishlistKosIdsAsLong(userId);

        assertEquals(1, result.size());
    }

    @Test
    void testGetUserWishlistKosIdsAsLong_NullKosId() {
        UUID userId = UUID.randomUUID();
        Kos kos = new Kos();
        kos.setId(null);
        
        Wishlist wishlist = new Wishlist("Test", userId.toString());
        wishlist.setKosList(Arrays.asList(kos));
        wishlistService.createWishlist(wishlist);

        List<Long> result = wishlistService.getUserWishlistKosIdsAsLong(userId);

        assertEquals(0, result.size());
    }

    @Test
    void testFilterDuplicateKos() {
        Kos kos1 = new Kos();
        kos1.setId(UUID.randomUUID());
        Kos kos2 = new Kos();
        kos2.setId(UUID.randomUUID());

        Wishlist wishlist = new Wishlist("Test", "user1");
        wishlist.setKosList(Arrays.asList(kos1, kos2, kos1)); // Duplicate

        Wishlist result = wishlistService.createWishlist(wishlist);

        assertEquals(2, result.getKosList().size());
    }

    @Test
    void testFilterDuplicateKos_WithNullKos() {
        Kos kos1 = new Kos();
        kos1.setId(UUID.randomUUID());

        Wishlist wishlist = new Wishlist("Test", "user1");
        wishlist.setKosList(Arrays.asList(kos1, null)); // Null kos

        Wishlist result = wishlistService.createWishlist(wishlist);

        assertEquals(1, result.getKosList().size());
    }
}