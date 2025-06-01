package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WishlistNotifier implements WishlistSubject {

    private final List<WishlistObserver> observers = new ArrayList<>();

    @Autowired
    public WishlistNotifier(ConsoleNotificationObserver consoleObserver, 
                           PushNotificationObserver pushObserver) {
        // Auto-register observers saat aplikasi start
        addObserver(consoleObserver);
        addObserver(pushObserver);
        System.out.println("🚀 WishlistNotifier initialized with " + observers.size() + " observers");
    }

    @Override
    public void addObserver(WishlistObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("✅ Observer added: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(WishlistObserver observer) {
        observers.remove(observer);
        System.out.println("❌ Observer removed: " + observer.getClass().getSimpleName());
    }

    @Override
    public void notifyObservers(Wishlist wishlist, String event) {
        System.out.println("🔔 Notifying " + observers.size() + " observers for event: " + event);
        for (WishlistObserver observer : observers) {
            try {
                observer.update(wishlist, event);
            } catch (Exception e) {
                System.err.println("Error notifying observer " + observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    public void clearObservers() {
        observers.clear();
        System.out.println("🧹 All observers cleared");
    }

    public int getObserverCount() {
        return observers.size();
    }
}