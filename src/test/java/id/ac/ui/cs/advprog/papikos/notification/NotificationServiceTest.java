package id.ac.ui.cs.advprog.papikos.notification;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.model.KosType;
import id.ac.ui.cs.advprog.papikos.model.NotificationType;
import id.ac.ui.cs.advprog.papikos.model.User;
import id.ac.ui.cs.advprog.papikos.wishlist.Wishlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;
    private Wishlist wishlist;
    private Kos kos;
    private User tenant1;
    private User tenant2;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        wishlist = new Wishlist();
        kos = new Kos("K001", "Kos SatoruMyBaby", KosType.CAMPUR);
        wishlist.addKos(kos);
        tenant1 = new User("u01", "tenant1@example.com", false);
        tenant2 = new User("u02", "tenant2@example.com", false);
    }

    @Test
    void testNotifyTenantsWhenRoomAvailable_Happy_NotificationSentToInterestedUsers() {
        List<User> interestedUsers = new ArrayList<>();
        interestedUsers.add(tenant1);
        wishlist.setInterestedUsers(interestedUsers);
        
        int sentCount = notificationService.notifyTenantsForAvailableRoom(wishlist, kos, NotificationType.KOS_AVAILABLE);
        assertEquals(1, sentCount);
    }


    @Test
    void testNotifyTenantsWhenRoomAvailable_Unhappy_NoNotificationSent() {
        Kos kosLain = new Kos("K999", "Kos Tidak Ada", KosType.PUTRA);
        int sentCount = notificationService.notifyTenantsForAvailableRoom(wishlist, kosLain, NotificationType.KOS_AVAILABLE);
        assertEquals(0, sentCount);
    }

    @Test
    void testAdminCanSendNotificationToAllUsers_Happy() {
        List<User> allUsers = new ArrayList<>();
        allUsers.add(new User("admin1", "admin@example.com", true));
        allUsers.add(new User("u01", "tenant1@example.com", false));
        allUsers.add(new User("u02", "tenant2@example.com", false));
        int sentCount = notificationService.sendNotificationToAllUsers(allUsers, "Selamat datang di PapiKos!", NotificationType.ADMIN_BROADCAST);
        assertEquals(3, sentCount);
    }
}
