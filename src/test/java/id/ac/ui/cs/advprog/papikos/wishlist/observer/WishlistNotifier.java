package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import java.util.ArrayList;
import java.util.List;

public class WishlistNotifier implements WishlistSubject {

    private final List<WishlistObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(WishlistObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(WishlistObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Wishlist wishlist, String event) {
        for (WishlistObserver observer : observers) {
            observer.update(wishlist, event);
        }
    }
}
