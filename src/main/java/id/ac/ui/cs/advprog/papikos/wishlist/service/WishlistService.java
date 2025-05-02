package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class WishlistService {

    private final List<Wishlist> wishlistStorage = new ArrayList<>();
    private final WishlistNotifier notifier;

    public WishlistService(WishlistNotifier notifier) {
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

    public List<Wishlist> getAllWishlists() {
        return new ArrayList<>(wishlistStorage);
    }

    public Wishlist getWishlistById(int id) {
        return wishlistStorage.stream()
            .filter(w -> Objects.equals(w.getId(), id))
            .findFirst()
            .orElse(null);
    }

    public boolean deleteWishlistById(int id) {
        return wishlistStorage.removeIf(w -> Objects.equals(w.getId(), id));
    }

    public boolean wishlistExists(String userId, String kosId) {
        return wishlistStorage.stream()
            .anyMatch(w -> Objects.equals(w.getUserId(), userId)
                        && Objects.equals(w.getKosId(), kosId));
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
