package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WishlistServiceTest {

    private WishlistService wishlistService;
    private UUID userId;
    private UUID kosId;
    private List<Wishlist> wishlistStorage;

    @Mock
    private WishlistNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        wishlistService = new WishlistService(notifier);

        userId = UUID.randomUUID();
        kosId = UUID.randomUUID();
        wishlistStorage = new ArrayList<>();
        ReflectionTestUtils.setField(wishlistService, "wishlistStorage", wishlistStorage);
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

        Long kosId = Math.abs(kos.getId().getMostSignificantBits()) % Long.MAX_VALUE;
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

        Long kosId = Math.abs(kos.getId().getMostSignificantBits()) % Long.MAX_VALUE;
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
        Long expectedKosId = Math.abs(kos.getId().getMostSignificantBits()) % Long.MAX_VALUE;
        assertEquals(expectedKosId, result.get(0));
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
        wishlist.setKosList(Arrays.asList(kos1, kos2, kos1));

        Wishlist result = wishlistService.createWishlist(wishlist);

        assertEquals(2, result.getKosList().size());
    }

    @Test
    void testFilterDuplicateKos_WithNullKos() {
        Kos kos1 = new Kos();
        kos1.setId(UUID.randomUUID());

        Wishlist wishlist = new Wishlist("Test", "user1");
        wishlist.setKosList(Arrays.asList(kos1, null));

        Wishlist result = wishlistService.createWishlist(wishlist);

        assertEquals(1, result.getKosList().size());
    }

    @Test
    void testToggleWishlistAddKosNoExistingWishlist() {
        wishlistService.toggleWishlist(userId, kosId);

        assertEquals(1, wishlistStorage.size(), "Wishlist should be created");
        Wishlist createdWishlist = wishlistStorage.get(0);
        assertEquals(userId.toString(), createdWishlist.getUserId(), "User ID should match");
        assertEquals("Wishlist " + userId, createdWishlist.getName(), "Wishlist name should be set correctly");
        assertEquals(1, createdWishlist.getKosList().size(), "Wishlist should contain one kos");
        assertEquals(kosId, createdWishlist.getKosList().get(0).getId(), "Kos ID should match");

        ArgumentCaptor<Wishlist> wishlistCaptor = ArgumentCaptor.forClass(Wishlist.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(notifier).notifyObservers(wishlistCaptor.capture(), actionCaptor.capture());

        assertEquals(createdWishlist, wishlistCaptor.getValue(), "Notified wishlist should match");
        assertEquals("added", actionCaptor.getValue(), "Action should be 'added'");
    }

    @Test
    void testToggleWishlistAddKosExistingWishlist() {
        Wishlist existingWishlist = new Wishlist();
        existingWishlist.setId(1);
        existingWishlist.setUserId(userId.toString());
        existingWishlist.setName("Existing Wishlist");
        existingWishlist.setKosList(new ArrayList<>());
        wishlistStorage.add(existingWishlist);

        wishlistService.toggleWishlist(userId, kosId);

        assertEquals(1, wishlistStorage.size(), "Wishlist count should remain the same");
        Wishlist updatedWishlist = wishlistStorage.get(0);
        assertEquals(1, updatedWishlist.getKosList().size(), "Wishlist should contain one kos");
        assertEquals(kosId, updatedWishlist.getKosList().get(0).getId(), "Kos ID should match");

        ArgumentCaptor<Wishlist> wishlistCaptor = ArgumentCaptor.forClass(Wishlist.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(notifier).notifyObservers(wishlistCaptor.capture(), actionCaptor.capture());

        assertEquals(updatedWishlist, wishlistCaptor.getValue(), "Notified wishlist should match");
        assertEquals("added", actionCaptor.getValue(), "Action should be 'added'");
    }

    @Test
    void testToggleWishlistRemoveKosExistingWishlist() {
        Wishlist existingWishlist = new Wishlist();
        existingWishlist.setId(1);
        existingWishlist.setUserId(userId.toString());
        existingWishlist.setName("Existing Wishlist");

        List<Kos> kosList = new ArrayList<>();
        Kos kos = new Kos();
        kos.setId(kosId);
        kosList.add(kos);
        existingWishlist.setKosList(kosList);

        wishlistStorage.add(existingWishlist);
        wishlistService.toggleWishlist(userId, kosId);

        assertEquals(1, wishlistStorage.size(), "Wishlist count should remain the same");
        Wishlist updatedWishlist = wishlistStorage.get(0);
        assertEquals(0, updatedWishlist.getKosList().size(), "Wishlist should not contain any kos");

        ArgumentCaptor<Wishlist> wishlistCaptor = ArgumentCaptor.forClass(Wishlist.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(notifier).notifyObservers(wishlistCaptor.capture(), actionCaptor.capture());

        assertEquals(updatedWishlist, wishlistCaptor.getValue(), "Notified wishlist should match");
        assertEquals("removed", actionCaptor.getValue(), "Action should be 'removed'");
    }

    @Test
    void testToggleWishlistKosWithNullIdExistingWishlist() {
        Wishlist existingWishlist = new Wishlist();
        existingWishlist.setId(1);
        existingWishlist.setUserId(userId.toString());
        existingWishlist.setName("Existing Wishlist");

        List<Kos> kosList = new ArrayList<>();
        Kos kosWithNullId = new Kos();
        kosWithNullId.setId(null);
        kosList.add(kosWithNullId);
        existingWishlist.setKosList(kosList);

        wishlistStorage.add(existingWishlist);
        wishlistService.toggleWishlist(userId, kosId);

        assertEquals(1, wishlistStorage.size(), "Wishlist count should remain the same");
        Wishlist updatedWishlist = wishlistStorage.get(0);
        assertEquals(2, updatedWishlist.getKosList().size(), "Wishlist should contain two kos items");

        boolean hasKosId = updatedWishlist.getKosList().stream()
                .anyMatch(k -> k.getId() != null && k.getId().equals(kosId));
        assertTrue(hasKosId, "Wishlist should contain a kos with the expected ID");
        verify(notifier).notifyObservers(any(Wishlist.class), eq("added"));
    }

    @Test
    void testToggleWishlistWithMultipleUsers() {
        UUID otherUserId = UUID.randomUUID();

        Wishlist otherUserWishlist = new Wishlist();
        otherUserWishlist.setId(1);
        otherUserWishlist.setUserId(otherUserId.toString());
        otherUserWishlist.setName("Other User Wishlist");
        otherUserWishlist.setKosList(new ArrayList<>());
        wishlistStorage.add(otherUserWishlist);

        wishlistService.toggleWishlist(userId, kosId);
        assertEquals(2, wishlistStorage.size(), "Should have two wishlists (one for each user)");

        Wishlist foundOtherUserWishlist = wishlistStorage.stream()
                .filter(w -> w.getUserId().equals(otherUserId.toString()))
                .findFirst()
                .orElse(null);

        Wishlist foundUserWishlist = wishlistStorage.stream()
                .filter(w -> w.getUserId().equals(userId.toString()))
                .findFirst()
                .orElse(null);

        assertNotNull(foundOtherUserWishlist, "Other user's wishlist should exist");
        assertNotNull(foundUserWishlist, "User's wishlist should exist");

        assertEquals(0, foundOtherUserWishlist.getKosList().size(), "Other user's wishlist should be empty");
        assertEquals(1, foundUserWishlist.getKosList().size(), "User's wishlist should have one kos");
        assertEquals(kosId, foundUserWishlist.getKosList().get(0).getId(), "Kos ID should match");
        verify(notifier).notifyObservers(eq(foundUserWishlist), eq("added"));
    }
}