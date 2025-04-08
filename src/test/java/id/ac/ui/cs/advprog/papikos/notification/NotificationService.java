package id.ac.ui.cs.advprog.papikos.notification;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.model.User;
import id.ac.ui.cs.advprog.papikos.wishlist.Wishlist;
import java.util.List;

public class NotificationService {

    
    public int notifyTenantsForAvailableRoom(Wishlist wishlist, Kos kos) {
        if (wishlist.getKosList().contains(kos)) {
            return 1;
        } else {
            return 0;
        }
    }

    
    public int sendNotificationToAllUsers(List<User> users, String message) {
        return users.size();
    }
}
