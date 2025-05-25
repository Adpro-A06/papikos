package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class WishlistService {

    private final List<Wishlist> wishlistStorage = new ArrayList<>();
    private final WishlistNotifier notifier;

    public WishlistService(WishlistNotifier notifier) {
        this.notifier = Objects.requireNonNull(notifier, "Notifier must not be null");
    }

    public Wishlist createWishlist(Wishlist wishlist) {
        Objects.requireNonNull(wishlist, "Wishlist must not be null");
        String name = wishlist.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Wishlist name is required");
        }
        List<Kos> kosList = wishlist.getKosList();
        if (kosList == null) {
            kosList = Collections.emptyList();
        }
        wishlist.setId(wishlistStorage.size() + 1);
        wishlist.setKosList(filterDuplicateKos(kosList));
        wishlistStorage.add(wishlist);
        notifier.notifyObservers(wishlist, "created");
        return wishlist;
    }

    public void addKosToWishlist(Integer wishlistId, String kosId) {
        Objects.requireNonNull(wishlistId, "Wishlist ID must not be null");
        Objects.requireNonNull(kosId, "Kos ID must not be null");
        Wishlist w = wishlistStorage.stream()
            .filter(x -> x.getId().equals(wishlistId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Wishlist not found: " + wishlistId));
        UUID kosUuid = UUID.fromString(kosId);
        boolean exists = w.getKosList().stream()
            .anyMatch(k -> kosUuid.equals(k.getId()));
        if (!exists) {
            Kos kos = new Kos();
            kos.setId(kosUuid);
            w.getKosList().add(kos);
            notifier.notifyObservers(w, "kosAdded");
        }
    }

    public Wishlist getUserWishlist(UUID userId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        return wishlistStorage.stream()
            .filter(x -> userId.equals(x.getUserId()))
            .findFirst()
            .orElse(null);
    }

    private List<Kos> filterDuplicateKos(List<Kos> kosList) {
        if (kosList == null) {
            return Collections.emptyList();
        }
        List<Kos> unique = new ArrayList<>();
        for (Kos k : kosList) {
            if (!unique.contains(k)) {
                unique.add(k);
            }
        }
        return unique;
    }
}
