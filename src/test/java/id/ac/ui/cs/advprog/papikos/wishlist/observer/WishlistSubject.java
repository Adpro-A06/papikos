package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;

public interface WishlistSubject {
    void addObserver(WishlistObserver observer);
    void removeObserver(WishlistObserver observer);
    void notifyObservers(Wishlist wishlist, String event);
}
