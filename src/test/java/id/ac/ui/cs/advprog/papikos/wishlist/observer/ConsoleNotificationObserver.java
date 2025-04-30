package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;

public class ConsoleNotificationObserver implements WishlistObserver {

    @Override
    public void update(Wishlist wishlist, String event) {
        System.out.println("Notification: Wishlist '" + wishlist.getName() + "' has been " + event);
    }
}
