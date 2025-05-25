package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Configuration
public class ObserverConfig {

    @Autowired
    public ObserverConfig(WishlistNotifier notifier, List<WishlistObserver> observers) {
        observers.forEach(notifier::addObserver);
    }
}
