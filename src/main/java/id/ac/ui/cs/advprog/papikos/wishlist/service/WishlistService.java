package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WishlistService {

    private final List<Wishlist> wishlistStorage = new ArrayList<>();
    private final WishlistNotifier notifier;

    public WishlistService(WishlistNotifier notifier) {
        this.notifier = notifier;
    }

    public Wishlist createWishlist(Wishlist wishlist) {
        if (wishlist.getName() == null || wishlist.getName().trim().isEmpty()) {
            return null;
        }

        // assign a new UUID as the wishlist ID
        wishlist.setId(UUID.randomUUID());

        // remove duplicates in the initial kos list
        List<Kos> filteredKos = filterDuplicateKos(wishlist.getKosList());
        wishlist.setKosList(filteredKos);

        wishlistStorage.add(wishlist);
        notifier.notifyObservers(wishlist, "created");
        return wishlist;
    }

    public void addKosToWishlist(UUID wishlistId, String kosId) {
        Wishlist wishlist = wishlistStorage.stream()
            .filter(w -> w.getId().equals(wishlistId))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Wishlist not found: " + wishlistId)
            );

        UUID kosUuid = UUID.fromString(kosId);
        boolean alreadyInList = wishlist.getKosList().stream()
            .anyMatch(k -> k.getId().equals(kosUuid));

        if (!alreadyInList) {
            Kos newKos = new Kos();
            newKos.setId(kosUuid);
            wishlist.getKosList().add(newKos);
            notifier.notifyObservers(wishlist, "kosAdded");
        }
    }

    public Wishlist getUserWishlist(UUID userId) {
        return wishlistStorage.stream()
            .filter(w -> w.getUserId().equals(userId))
            .findFirst()
            .orElse(null);
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
