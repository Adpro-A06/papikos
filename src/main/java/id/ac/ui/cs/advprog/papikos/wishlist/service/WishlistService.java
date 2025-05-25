package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import id.ac.ui.cs.advprog.papikos.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistRepository repository;
    private final WishlistNotifier notifier;

    public WishlistService(
        WishlistRepository repository,
        WishlistNotifier notifier
    ) {
        this.repository = repository;
        this.notifier = notifier;
    }

    public Wishlist createWishlist(Wishlist wishlist) {
        if (wishlist == null
            || wishlist.getName() == null
            || wishlist.getName().trim().isEmpty()
        ) {
            return null;
        }
        wishlist.setKosList(removeDuplicates(wishlist.getKosList()));
        Wishlist saved = repository.save(wishlist);
        notifier.notifyObservers(saved, "created");
        return saved;
    }

    public Wishlist addKosToWishlist(Long wishlistId, String kosId) {
        Wishlist w = repository.findById(wishlistId)
            .orElseThrow(() ->
                new IllegalArgumentException("Wishlist not found: " + wishlistId)
            );
        UUID uuid = UUID.fromString(kosId);
        if (w.getKosList().stream().noneMatch(k -> k.getId().equals(uuid))) {
            Kos newKos = new Kos();
            newKos.setId(uuid);
            w.getKosList().add(newKos);
            w.setKosList(removeDuplicates(w.getKosList()));
            w = repository.save(w);
            notifier.notifyObservers(w, "kosAdded");
        }
        return w;
    }

    public List<Wishlist> getUserWishlist(UUID userId) {
        return repository.findByUserId(userId);
    }

    private List<Kos> removeDuplicates(List<Kos> kosList) {
        if (kosList == null) return List.of();
        return kosList.stream()
            .distinct()
            .collect(Collectors.toList());
    }
}
