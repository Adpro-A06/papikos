package id.ac.ui.cs.advprog.papikos.notification;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.model.Notification;
import id.ac.ui.cs.advprog.papikos.model.NotificationType;
import id.ac.ui.cs.advprog.papikos.model.User;
import id.ac.ui.cs.advprog.papikos.wishlist.Wishlist;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationService {

    private final List<Notification> notifications = new ArrayList<>();

    public int notifyTenantsForAvailableRoom(Wishlist wishlist, Kos kos, NotificationType type) {
        if (type != NotificationType.KOS_AVAILABLE) {
            throw new IllegalArgumentException("Invalid notification type");
        }
        int count = 0;
        if (wishlist.getKosList().contains(kos)) {
            for (User user : wishlist.getInterestedUsers()) {
                Notification notification = new Notification(
                    UUID.randomUUID().toString(),
                    "Kos " + kos.getName() + " sekarang tersedia",
                    type,
                    user
                );
                notifications.add(notification);
                count++;
            }
        }
        return count;
    }

    public int sendNotificationToAllUsers(List<User> users, String message, NotificationType type) {
        if (type != NotificationType.ADMIN_BROADCAST) {
            throw new IllegalArgumentException("Invalid notification type");
        }
        for (User user : users) {
            Notification notification = new Notification(
                UUID.randomUUID().toString(),
                message,
                type,
                user
            );
            notifications.add(notification);
        }
        return users.size();
    }

    public List<Notification> getAllNotifications() {
        return notifications;
    }
}
