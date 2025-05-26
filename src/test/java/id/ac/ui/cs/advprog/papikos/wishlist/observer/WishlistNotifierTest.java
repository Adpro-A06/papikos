package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WishlistNotifierTest {

    private WishlistNotifier notifier;
    
    @Mock
    private WishlistObserver mockObserver1;
    
    @Mock
    private WishlistObserver mockObserver2;
    
    private Wishlist testWishlist;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notifier = WishlistNotifier.getInstance(); 
        notifier.clearObservers(); 
        testWishlist = new Wishlist("Test", "user123");
    }

    @Test
    void testAddObserver() {
        notifier.addObserver(mockObserver1); 
        notifier.notifyObservers(testWishlist, "created");
        
        verify(mockObserver1).update(testWishlist, "created");
    }

    @Test
    void testRemoveObserver() {
        notifier.addObserver(mockObserver1);
        notifier.removeObserver(mockObserver1); 
        notifier.notifyObservers(testWishlist, "created");
        
        verify(mockObserver1, never()).update(any(), any());
    }

    @Test
    void testNotifyMultipleObservers() {
        notifier.addObserver(mockObserver1);
        notifier.addObserver(mockObserver2);
        
        notifier.notifyObservers(testWishlist, "added");
        
        verify(mockObserver1).update(testWishlist, "added");
        verify(mockObserver2).update(testWishlist, "added");
    }

    @Test
    void testNotifyWithNullWishlist() {
        notifier.addObserver(mockObserver1);
        
        notifier.notifyObservers(null, "created");
        
        verify(mockObserver1).update(null, "created");
    }

    @Test
    void testNotifyWithNullAction() {
        notifier.addObserver(mockObserver1);
        
        notifier.notifyObservers(testWishlist, null);
        
        verify(mockObserver1).update(testWishlist, null);
    }

    @Test
    void testAddSameObserverTwice() {
        notifier.addObserver(mockObserver1);
        notifier.addObserver(mockObserver1);
        
        notifier.notifyObservers(testWishlist, "created");
        
        verify(mockObserver1, times(2)).update(testWishlist, "created");
    }

    @Test
    void testRemoveNonExistentObserver() {
        notifier.removeObserver(mockObserver1);
        notifier.notifyObservers(testWishlist, "created");
        
        verify(mockObserver1, never()).update(any(), any());
    }

    @Test
    void testNotifyWithNoObservers() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            notifier.notifyObservers(testWishlist, "created");
        });
    }

    @Test
    void testSingletonInstance() {
        WishlistNotifier instance1 = WishlistNotifier.getInstance();
        WishlistNotifier instance2 = WishlistNotifier.getInstance();
        
        assertSame(instance1, instance2);
    }

    @Test
    void testClearObservers() {
        notifier.addObserver(mockObserver1);
        notifier.addObserver(mockObserver2);
        
        notifier.clearObservers();
        notifier.notifyObservers(testWishlist, "test");
        
        verify(mockObserver1, never()).update(any(), any());
        verify(mockObserver2, never()).update(any(), any());
    }

    @Test
    void testNotifier_ExtremeEdgeCases() {
        // Test dengan very long strings
        Wishlist longWishlist = new Wishlist("Very long wishlist name that should test string handling capabilities", "user-with-very-long-id-that-might-cause-issues");
        
        notifier.addObserver(mockObserver1);
        notifier.notifyObservers(longWishlist, "action-with-very-long-name-to-test-edge-cases");
        
        verify(mockObserver1).update(longWishlist, "action-with-very-long-name-to-test-edge-cases");
    }

    @Test
    void testNotifier_SpecialCharacters() {
        Wishlist specialWishlist = new Wishlist("Special chars: àáâãäåæçèéêë", "user@#$%^&*()");
        
        notifier.addObserver(mockObserver1);
        notifier.notifyObservers(specialWishlist, "special!@#$%");
        
        verify(mockObserver1).update(specialWishlist, "special!@#$%");
    }

    @Test
    void testNotifier_EmptyStrings() {
        Wishlist emptyWishlist = new Wishlist("", "");
        
        notifier.addObserver(mockObserver1);
        notifier.notifyObservers(emptyWishlist, "");
        
        verify(mockObserver1).update(emptyWishlist, "");
    }

    


    @Test
    void testNotifier_RemoveNullObserver() {
        // Test remove null observer
        assertDoesNotThrow(() -> {
            notifier.removeObserver(null);
        });
    }

    @Test
    void testNotifier_MultipleAddRemove() {
        // Complex add/remove pattern
        notifier.addObserver(mockObserver1);
        notifier.addObserver(mockObserver2);
        notifier.addObserver(mockObserver1); 
        
        notifier.removeObserver(mockObserver1);
        notifier.notifyObservers(testWishlist, "complex");
        
        verify(mockObserver1, times(1)).update(testWishlist, "complex"); // Should still be called once due to duplicate
        verify(mockObserver2).update(testWishlist, "complex");
    }

    @Test
    void testNotifier_ClearAllObservers() {
        notifier.addObserver(mockObserver1);
        notifier.addObserver(mockObserver2);
        
        notifier.clearObservers();
        notifier.notifyObservers(testWishlist, "cleared");
        
        verify(mockObserver1, never()).update(any(), any());
        verify(mockObserver2, never()).update(any(), any());
    }

    @Test
    void testNotifier_LargeNumberOfObservers() {
        
        for (int i = 0; i < 100; i++) {
            notifier.addObserver(mockObserver1);
        }
        
        notifier.notifyObservers(testWishlist, "stress-test");
        
        verify(mockObserver1, times(100)).update(testWishlist, "stress-test");
    }

    @Test
    void testNotifier_ObserverOrder() {
        notifier.addObserver(mockObserver1);
        notifier.addObserver(mockObserver2);
        
        notifier.notifyObservers(testWishlist, "order-test");
        
        // Both should be called
        verify(mockObserver1).update(testWishlist, "order-test");
        verify(mockObserver2).update(testWishlist, "order-test");
    }
}