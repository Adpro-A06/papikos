package id.ac.ui.cs.advprog.papikos.wishlist;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import id.ac.ui.cs.advprog.papikos.model.Kos;
import java.util.ArrayList;
import java.util.List;

public class wishlistService {

    private List<Wishlist> wishlistStorage = new ArrayList<>();
    private WishlistNotifier notifier;

    public wishlistService(WishlistNotifier notifier) {
        this.notifier = notifier;
    }

    public Wishlist createWishlist(Wishlist wishlist) {
        if (wishlist.getName() == null || wishlist.getName().trim().isEmpty()) {
            return null;
        }

        
        wishlist.setId(wishlistStorage.size() + 1);

        
        List<Kos> kosUniq = new ArrayList<>();
        for (Kos item : wishlist.getKosList()) {
            if (!kosUniq.contains(item)) {
                kosUniq.add(item);
            }
        }
        
        
        wishlist.setKosList(kosUniq);

        wishlistStorage.add(wishlist);

        notifier.notifyObservers(wishlist, "created");

        return wishlist;
    }
}
