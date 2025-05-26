package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WishlistNotifier implements WishlistSubject {

    private static WishlistNotifier instance;
    private final List<WishlistObserver> observers = new ArrayList<>();

    private WishlistNotifier() {}

    public static synchronized WishlistNotifier getInstance() {
        if (instance == null) {
            instance = new WishlistNotifier();
        }
        return instance;
    }

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

    public void clearObservers() {
        observers.clear();
    }
    
}
