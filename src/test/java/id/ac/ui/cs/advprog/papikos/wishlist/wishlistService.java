package id.ac.ui.cs.advprog.papikos.wishlist;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import java.util.ArrayList;
import java.util.List;

public class wishlistService {

    private final List<Wishlist> wishlistStorage = new ArrayList<>();
    private final WishlistNotifier notifier;

    public wishlistService(WishlistNotifier notifier) {
        this.notifier = notifier;
    }

    public Wishlist createWishlist(Wishlist wishlist) {
        if (!isValidWishlist(wishlist)) {
            return null;
        }

        
        wishlist.setId(wishlistStorage.size() + 1);

        
        List<Kos> filteredKos = filterDuplicateKos(wishlist.getKosList());
        wishlist.setKosList(filteredKos);

        wishlistStorage.add(wishlist);

        notifier.notifyObservers(wishlist, "created");

        return wishlist;
    }

    private boolean isValidWishlist(Wishlist wishlist) {
        return wishlist.getName() != null && !wishlist.getName().trim().isEmpty();
    }

    private List<Kos> filterDuplicateKos(List<Kos> kosList) {
        List<Kos> uniqueKos = new ArrayList<>();
        for (Kos kos : kosList) {
            if (!uniqueKos.contains(kos)) {
                uniqueKos.add(kos);
            }
        }
        return uniqueKos;
    }
}
