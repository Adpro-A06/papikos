package id.ac.ui.cs.advprog.papikos;

import id.ac.ui.cs.advprog.papikos.wishlist.wishlistService;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.ConsoleNotificationObserver;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;

public class Main {

    public static void main(String[] args) {
       

        WishlistNotifier notifier = new WishlistNotifier();
        notifier.addObserver(new ConsoleNotificationObserver());

        

        wishlistService wishlistService = new wishlistService(notifier);

       
        Wishlist wishlist = new Wishlist("My Favourite Kos");
        wishlist.setUserId("user1");
        wishlist.setKosId("kos1");

        wishlistService.createWishlist(wishlist);
    }
}
