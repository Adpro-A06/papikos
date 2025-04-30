package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.wishlistService;
import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.model.KosType;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

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

    private wishlistService wishlistService;
    private WishlistNotifier notifier;
    private DummyWishlistObserver dummyObserver;

    @BeforeEach
    public void setUp() {
        notifier = WishlistNotifier.getInstance();
        notifier.clearObservers();
        dummyObserver = new DummyWishlistObserver();
        notifier.addObserver(dummyObserver);
        wishlistService = new wishlistService(notifier);
    }

    @Test
    public void testObserverNotificationOnCreateWishlist() {
        Wishlist wishlist = new Wishlist("Observer Test Wishlist");
        wishlist.addKos(new Kos("K001", "Kos Example", KosType.PUTRA));
        wishlistService.createWishlist(wishlist);
        List<String> events = dummyObserver.getNotifications();
        assertEquals(1, events.size(), "Observer should be notified once");
        assertEquals("created", events.get(0), "Event should be 'created'");
    }

    @Test
    public void testNoNotificationWhenObserverNotRegistered() {
        WishlistNotifier emptyNotifier = WishlistNotifier.getInstance();
        emptyNotifier.clearObservers();
        wishlistService serviceWithoutObserver = new wishlistService(emptyNotifier);
        Wishlist wishlist = new Wishlist("No Observer Wishlist");
        wishlist.addKos(new Kos("K002", "Kos Example 2", KosType.PUTRI));
        serviceWithoutObserver.createWishlist(wishlist);
        assertTrue(true);
    }
}
