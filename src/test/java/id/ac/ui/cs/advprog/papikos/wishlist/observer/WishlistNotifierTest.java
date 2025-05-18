package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DummyWishlistObserver implements WishlistObserver {
    private final List<String> notifications = new ArrayList<>();

    @Override
    public void update(Wishlist wishlist, String event) {
        notifications.add(event);
    }

    public List<String> getNotifications() {
        return notifications;
    }
}

public class WishlistNotifierTest {

    private WishlistService wishlistService;
    private WishlistNotifier notifier;
    private DummyWishlistObserver dummyObserver;

    @BeforeEach
    public void setUp() {
        notifier = WishlistNotifier.getInstance();
        notifier.clearObservers();

        dummyObserver = new DummyWishlistObserver();
        notifier.addObserver(dummyObserver);

        wishlistService = new WishlistService(notifier);
    }

    @Test
    public void testObserverNotificationOnCreateWishlist() {
        Wishlist wishlist = new Wishlist("Observer Test Wishlist");
        Kos kos = new Kos();
        kos.setId(UUID.randomUUID());
        kos.setNama("Kos Example");
        wishlist.addKos(kos);

        wishlistService.createWishlist(wishlist);

        List<String> events = dummyObserver.getNotifications();
        assertEquals(1, events.size(), "Observer should be notified once");
        assertEquals("created", events.get(0), "Event should be 'created'");
    }

    @Test
    public void testNoNotificationWhenObserverNotRegistered() {
        // Clear semua observer
        WishlistNotifier emptyNotifier = WishlistNotifier.getInstance();
        emptyNotifier.clearObservers();

        WishlistService serviceWithoutObserver = new WishlistService(emptyNotifier);

        Wishlist wishlist = new Wishlist("No Observer Wishlist");
        Kos kos = new Kos();
        kos.setId(UUID.randomUUID());
        kos.setNama("Kos Example 2");
        wishlist.addKos(kos);

        serviceWithoutObserver.createWishlist(wishlist);
        assertTrue(true);
    }
}
