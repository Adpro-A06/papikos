package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;

public interface WishlistObserver {
    void update(Wishlist wishlist, String event);
}
