package id.ac.ui.cs.advprog.papikos.notification.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.notification.model.NotificationType;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
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

        kos = new Kos();
        kos.setId("K001");
        kos.setNama("Kos SatoruMyBaby");

        wishlist.addKos(kos);

        tenant1 = new User("tenant1@example.com", "P@ssword1!", Role.PENYEWA);
        tenant2 = new User("tenant2@example.com", "P@ssword2!", Role.PENYEWA);
    }

    @Test
    void testNotifyTenantsWhenRoomAvailable_Happy_NotificationSentToInterestedUsers() {
        List<User> interestedUsers = new ArrayList<>();
        interestedUsers.add(tenant1);
        wishlist.setInterestedUsers(interestedUsers);

        int sentCount = notificationService.notifyTenantsForAvailableRoom(
                wishlist,
                kos,
                NotificationType.KOS_AVAILABLE
        );
        assertEquals(1, sentCount);
    }

    @Test
    void testNotifyTenantsWhenRoomAvailable_Unhappy_NoNotificationSent() {
        Kos kosLain = new Kos();
        kosLain.setId("K999");
        kosLain.setNama("Kos Tidak Ada");

        int sentCount = notificationService.notifyTenantsForAvailableRoom(
                wishlist,
                kosLain,
                NotificationType.KOS_AVAILABLE
        );
        assertEquals(0, sentCount);
    }

    @Test
    void testAdminCanSendNotificationToAllUsers_Happy() {
        List<User> allUsers = new ArrayList<>();
        // Admin
        User admin = new User("admin@example.com", "Admin@123!", Role.ADMIN);
        allUsers.add(admin);
        // Dua tenant
        allUsers.add(tenant1);
        allUsers.add(tenant2);

        int sentCount = notificationService.sendNotificationToAllUsers(
                allUsers,
                "Selamat datang di PapiKos!",
                NotificationType.ADMIN_BROADCAST
        );
        assertEquals(3, sentCount);
    }
}