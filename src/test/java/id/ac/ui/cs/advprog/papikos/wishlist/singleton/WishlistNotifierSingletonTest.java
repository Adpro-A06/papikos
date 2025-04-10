package id.ac.ui.cs.advprog.papikos.wishlist.singleton;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;

public class WishlistNotifierSingletonTest {
    @Test
    public void testSingletonInstance() {
        WishlistNotifier instance1 = WishlistNotifier.getInstance();
        WishlistNotifier instance2 = WishlistNotifier.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }
}
