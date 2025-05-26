package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistNotifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public boolean isInWishlist(UUID userId, Long kosId) {
        return wishlistStorage.stream()
                .anyMatch(w -> w.getUserId().equals(userId.toString()) && 
                         w.getKosList().stream().anyMatch(k -> {
                             // Convert UUID to Long for comparison
                             UUID kosUuid = k.getId();
                             Long kosLongId = Math.abs(kosUuid.hashCode()) % Long.MAX_VALUE;
                             return kosLongId.equals(kosId);
                         }));
    }

    public Wishlist addToWishlist(Wishlist wishlist) {
        // Check if user already has this kos in wishlist
        boolean exists = wishlistStorage.stream()
                .anyMatch(w -> w.getUserId().equals(wishlist.getUserId()) && 
                         w.getKosList().stream().anyMatch(k -> 
                             wishlist.getKosList().stream().anyMatch(newK -> 
                                 k.getId().equals(newK.getId()))));
        
        if (!exists) {
            wishlist.setId(wishlistStorage.size() + 1);
            wishlistStorage.add(wishlist);
            notifier.notifyObservers(wishlist, "added");
        }
        return wishlist;
    }

    public boolean removeFromWishlist(UUID userId, Long kosId) {
        return wishlistStorage.removeIf(w -> 
            w.getUserId().equals(userId.toString()) && 
            w.getKosList().stream().anyMatch(k -> {
                // Convert UUID to Long for comparison
                UUID kosUuid = k.getId();
                Long kosLongId = Math.abs(kosUuid.hashCode()) % Long.MAX_VALUE;
                return kosLongId.equals(kosId);
            }));
    }

    public int getWishlistCount(UUID userId) {
        return (int) wishlistStorage.stream()
                .filter(w -> w.getUserId().equals(userId.toString()))
                .mapToInt(w -> w.getKosList().size())
                .sum();
    }

    public void clearUserWishlist(UUID userId) {
        wishlistStorage.removeIf(w -> w.getUserId().equals(userId.toString()));
    }

    public List<Wishlist> getUserWishlist(UUID userId) {
        return wishlistStorage.stream()
                .filter(w -> w.getUserId().equals(userId.toString()))
                .collect(Collectors.toList());
    }

    public List<Long> getUserWishlistKosIdsAsLong(UUID userId) {
        return wishlistStorage.stream()
                .filter(w -> w.getUserId().equals(userId.toString()))
                .flatMap(w -> w.getKosList().stream())
                .map(kos -> {
                    // Convert UUID to Long using hashCode
                    UUID kosUuid = kos.getId();
                    return Math.abs(kosUuid.hashCode()) % Long.MAX_VALUE;
                })
                .collect(Collectors.toList());
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