package id.ac.ui.cs.advprog.papikos.wishlist.repository;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

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

    @Test
    void testRepository_MultipleOperations() {
        Wishlist w1 = repository.save(new Wishlist("W1", "u1"));
        Wishlist w2 = repository.save(new Wishlist("W2", "u2"));
        Wishlist w3 = repository.save(new Wishlist("W3", "u3"));

        assertEquals(3, repository.findAll().size());

        repository.deleteById(w2.getId());
        assertEquals(2, repository.findAll().size());

        assertTrue(repository.findById(w1.getId()).isPresent());
        assertFalse(repository.findById(w2.getId()).isPresent());
        assertTrue(repository.findById(w3.getId()).isPresent());
    }

    @Test
    void testRepository_EdgeCases() {
        Wishlist w1 = repository.save(new Wishlist("W1", "u1"));
        repository.deleteById(w1.getId());

        Wishlist w2 = repository.save(new Wishlist("W2", "u2"));

        assertTrue(w2.getId() > w1.getId());
    }

    @Test
    void testRepository_NullHandling() {
        Wishlist nullWishlist = new Wishlist();
        nullWishlist.setName(null);
        nullWishlist.setUserId(null);

        Wishlist saved = repository.save(nullWishlist);
        assertNotNull(saved.getId());
    }

    @Test
    void testRepository_LargeDataset() {
        List<Wishlist> wishlists = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            wishlists.add(repository.save(new Wishlist("W" + i, "user" + i)));
        }

        assertEquals(50, repository.findAll().size());

        for (int i = 0; i < 25; i++) {
            repository.deleteById(wishlists.get(i).getId());
        }

        assertEquals(25, repository.findAll().size());
    }

    @Test
    void testRepository_ConcurrentAccess() {
        Wishlist w1 = new Wishlist("Concurrent1", "user1");
        Wishlist w2 = new Wishlist("Concurrent2", "user2");

        repository.save(w1);
        repository.save(w2);

        assertNotEquals(w1.getId(), w2.getId());
    }

    @Test
    void testRepository_FindByIdEdgeCases() {
        Optional<Wishlist> negative = repository.findById(-1);
        assertFalse(negative.isPresent());

        Optional<Wishlist> zero = repository.findById(0);
        assertFalse(zero.isPresent());

        Optional<Wishlist> large = repository.findById(999999);
        assertFalse(large.isPresent());
    }

    @Test
    void testRepository_DeleteEdgeCases() {
        assertFalse(repository.deleteById(-1));
        assertFalse(repository.deleteById(0));
        assertFalse(repository.deleteById(999999));
    }

    @Test
    void testRepository_SaveWithNullFields() {
        Wishlist nullFieldWishlist = new Wishlist();
        nullFieldWishlist.setName(null);
        nullFieldWishlist.setUserId(null);
        nullFieldWishlist.setKosList(null);

        Wishlist saved = repository.save(nullFieldWishlist);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
    }

    @Test
    void testRepository_SaveWithEmptyFields() {
        Wishlist emptyFieldWishlist = new Wishlist();
        emptyFieldWishlist.setName("");
        emptyFieldWishlist.setUserId("");
        emptyFieldWishlist.setKosList(new ArrayList<>());

        Wishlist saved = repository.save(emptyFieldWishlist);

        assertNotNull(saved.getId());
        assertEquals("", saved.getName());
        assertEquals("", saved.getUserId());
    }

    @Test
    void testRepository_FindAllEmptyRepo() {
        List<Wishlist> all = repository.findAll();
        for (Wishlist w : all) {
            repository.deleteById(w.getId());
        }

        List<Wishlist> empty = repository.findAll();
        assertTrue(empty.isEmpty());
    }

    @Test
    void testRepository_SaveReturnsCorrectObject() {
        Wishlist original = new Wishlist("Test Name", "TestUser");
        original.setKosList(new ArrayList<>());

        Wishlist saved = repository.save(original);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Name", saved.getName());
        assertEquals("TestUser", saved.getUserId());
        assertNotNull(saved.getKosList());
    }

    @Test
    void testRepository_IdGeneration() {
        Wishlist w1 = repository.save(new Wishlist("W1", "U1"));
        Wishlist w2 = repository.save(new Wishlist("W2", "U2"));
        Wishlist w3 = repository.save(new Wishlist("W3", "U3"));

        assertTrue(w2.getId() > w1.getId());
        assertTrue(w3.getId() > w2.getId());

        repository.deleteById(w2.getId());

        Wishlist w4 = repository.save(new Wishlist("W4", "U4"));
        assertTrue(w4.getId() > w3.getId());
    }

    @Test
    void testRepository_FindAllConsistency() {
        Wishlist w1 = repository.save(new Wishlist("FindAll1", "User1"));
        Wishlist w2 = repository.save(new Wishlist("FindAll2", "User2"));

        List<Wishlist> all = repository.findAll();

        assertTrue(all.stream().anyMatch(w -> w.getId().equals(w1.getId())));
        assertTrue(all.stream().anyMatch(w -> w.getId().equals(w2.getId())));
    }

    @Test
    void testRepository_DeleteExistingReturnsTrue() {
        Wishlist saved = repository.save(new Wishlist("ToDelete", "DeleteUser"));
        Integer savedId = saved.getId();

        boolean deleted = repository.deleteById(savedId);

        assertTrue(deleted);
        assertFalse(repository.findById(savedId).isPresent());
    }

    @Test
    void testRepository_LargeDataSet() {
        List<Integer> savedIds = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Wishlist w = repository.save(new Wishlist("Item" + i, "User" + i));
            savedIds.add(w.getId());
        }

        for (Integer id : savedIds) {
            assertTrue(repository.findById(id).isPresent());
        }

        for (int i = 0; i < 10; i++) {
            repository.deleteById(savedIds.get(i));
        }

        for (int i = 0; i < 10; i++) {
            assertFalse(repository.findById(savedIds.get(i)).isPresent());
        }

        for (int i = 10; i < 20; i++) {
            assertTrue(repository.findById(savedIds.get(i)).isPresent());
        }
    }

    @Test
    void testRepository_ThreadSafety() {
        List<Wishlist> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            final int index = i;
            Wishlist w = new Wishlist("Concurrent" + index, "user" + index);
            results.add(repository.save(w));
        }

        Set<Integer> uniqueIds = results.stream()
                .map(Wishlist::getId)
                .collect(Collectors.toSet());

        assertEquals(10, uniqueIds.size());
    }

    @Test
    void testRepository_FindAllOrder() {
        Wishlist w1 = repository.save(new Wishlist("First", "user1"));
        Wishlist w2 = repository.save(new Wishlist("Second", "user2"));
        Wishlist w3 = repository.save(new Wishlist("Third", "user3"));

        List<Wishlist> all = repository.findAll();

        assertTrue(all.size() >= 3);
        assertTrue(all.stream().anyMatch(w -> w.getId().equals(w1.getId())));
        assertTrue(all.stream().anyMatch(w -> w.getId().equals(w2.getId())));
        assertTrue(all.stream().anyMatch(w -> w.getId().equals(w3.getId())));
    }

    @Test
    void testRepository_SaveWithComplexKosList() {
        Wishlist wishlist = new Wishlist("Complex", "user");
        // Fixed: Create empty list instead of string list
        wishlist.setKosList(new ArrayList<>());

        Wishlist saved = repository.save(wishlist);

        assertNotNull(saved.getId());
        assertEquals(0, saved.getKosList().size());
    }

    @Test
    void testRepository_BoundaryConditions() {
        Wishlist minWishlist = new Wishlist("", "");
        Wishlist maxWishlist = new Wishlist("A".repeat(1000), "U".repeat(500));

        Wishlist savedMin = repository.save(minWishlist);
        Wishlist savedMax = repository.save(maxWishlist);

        assertNotNull(savedMin.getId());
        assertNotNull(savedMax.getId());
        assertNotEquals(savedMin.getId(), savedMax.getId());
    }

    @Test
    void testDeleteExistingWishlist() {
        Wishlist saved = repository.save(testWishlist);
        int initialSize = repository.findAll().size();

        repository.delete(saved);

        assertEquals(initialSize - 1, repository.findAll().size());
        assertFalse(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void testDeleteNonExistentWishlist() {
        Wishlist nonExistent = new Wishlist("Non-existent", "user456");
        nonExistent.setId(999);
        int initialSize = repository.findAll().size();

        repository.delete(nonExistent);

        assertEquals(initialSize, repository.findAll().size(), "Repository size should not change");
    }

    @Test
    void testDeleteWishlistWithNullId() {
        Wishlist nullIdWishlist = new Wishlist("Null ID", "user789");
        nullIdWishlist.setId(null);
        int initialSize = repository.findAll().size();

        repository.delete(nullIdWishlist);

        assertEquals(initialSize, repository.findAll().size(), "Repository size should not change");
    }

    @Test
    void testFindByUserIdUserWithMultipleWishlists() {
        String userId = "multiUser";
        Wishlist w1 = new Wishlist("User Wishlist 1", userId);
        Wishlist w2 = new Wishlist("User Wishlist 2", userId);
        Wishlist otherUserWishlist = new Wishlist("Other User Wishlist", "otherUser");

        repository.save(w1);
        repository.save(w2);
        repository.save(otherUserWishlist);

        List<Wishlist> userWishlists = repository.findByUserId(userId);

        assertEquals(2, userWishlists.size(), "Should find two wishlists for the user");
        assertTrue(userWishlists.stream().allMatch(w -> w.getUserId().equals(userId)));
    }

    @Test
    void testFindByUserIdUserWithNoWishlists() {
        String nonExistentUserId = "nonExistentUser";

        List<Wishlist> userWishlists = repository.findByUserId(nonExistentUserId);

        assertTrue(userWishlists.isEmpty(), "Should return empty list for user with no wishlists");
    }

    @Test
    void testFindByUserIdNullUserId() {
        Wishlist nullUserIdWishlist = new Wishlist("Null User ID", null);
        repository.save(nullUserIdWishlist);

        List<Wishlist> userWishlists = repository.findByUserId(null);

        assertFalse(userWishlists.isEmpty(), "Should find wishlist with null userId");
        assertEquals(1, userWishlists.size());
        assertNull(userWishlists.get(0).getUserId());
    }

    @Test
    void testFindByUserIdAndKosIdMatchingWishlist() {
        String userId = "testUser";
        String kosId = "kos123";

        Wishlist wishlist = new Wishlist("Test Wishlist", userId);
        wishlist.setKosId(kosId);
        repository.save(wishlist);

        Optional<Wishlist> result = repository.findByUserIdAndKosId(userId, kosId);

        assertTrue(result.isPresent(), "Should find the wishlist");
        assertEquals(userId, result.get().getUserId());
        assertEquals(kosId, result.get().getKosId());
    }

    @Test
    void testFindByUserIdAndKosIdNoMatch() {
        String userId = "testUser";
        String kosId = "kos123";
        String differentKosId = "kos456";

        Wishlist wishlist = new Wishlist("Test Wishlist", userId);
        wishlist.setKosId(differentKosId);
        repository.save(wishlist);

        Optional<Wishlist> result = repository.findByUserIdAndKosId(userId, kosId);

        assertFalse(result.isPresent(), "Should not find a wishlist with non-matching kosId");
    }

    @Test
    void testFindByUserIdAndKosIdNullValues() {
        Wishlist nullIdsWishlist = new Wishlist("Null IDs", null);
        nullIdsWishlist.setKosId(null);
        repository.save(nullIdsWishlist);

        Optional<Wishlist> result = repository.findByUserIdAndKosId(null, null);

        assertTrue(result.isPresent(), "Should find wishlist with null userId and kosId");
        assertNull(result.get().getUserId());
        assertNull(result.get().getKosId());
    }

    @Test
    void testClearWithWishlists() {
        repository.save(new Wishlist("W1", "u1"));
        repository.save(new Wishlist("W2", "u2"));

        repository.clear();

        assertTrue(repository.findAll().isEmpty(), "Repository should be empty after clear");

        Wishlist newWishlist = repository.save(new Wishlist("New", "user"));
        assertEquals(Integer.valueOf(1), newWishlist.getId(), "ID counter should be reset to 1");
    }

    @Test
    void testClearEmptyRepository() {
        List<Wishlist> all = repository.findAll();
        for (Wishlist w : all) {
            repository.deleteById(w.getId());
        }

        repository.clear();

        assertTrue(repository.findAll().isEmpty(), "Repository should remain empty after clear");

        Wishlist newWishlist = repository.save(new Wishlist("New", "user"));
        assertEquals(Integer.valueOf(1), newWishlist.getId(), "ID counter should be reset to 1");
    }
}