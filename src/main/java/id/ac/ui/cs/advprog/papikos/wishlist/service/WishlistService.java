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

        List<Kos> kosList = wishlist.getKosList();
        if (kosList != null) {
            List<Kos> filteredKos = filterDuplicateKos(kosList);
            wishlist.setKosList(filteredKos);
        } else {
            wishlist.setKosList(new ArrayList<>());
        }

        wishlistStorage.add(wishlist);
        notifier.notifyObservers(wishlist, "created");
        return wishlist;
    }

    public boolean isInWishlist(UUID userId, Long kosId) {
        return wishlistStorage.stream()
                .anyMatch(w -> w.getUserId().equals(userId.toString()) &&
                        w.getKosList().stream().anyMatch(k -> {
                            if (k.getId() == null)
                                return false;
                            UUID kosUuid = k.getId();
                            Long kosLongId = Math.abs(kosUuid.getMostSignificantBits()) % Long.MAX_VALUE;
                            return kosLongId.equals(kosId);
                        }));
    }

    public Wishlist addToWishlist(Wishlist wishlist) {

        if (wishlist.getKosList() == null) {
            wishlist.setKosList(new ArrayList<>());
        }

        boolean exists = wishlistStorage.stream()
                .anyMatch(w -> w.getUserId().equals(wishlist.getUserId()) &&
                        w.getKosList().stream().anyMatch(k -> wishlist.getKosList().stream().anyMatch(
                                newK -> k.getId() != null && newK.getId() != null && k.getId().equals(newK.getId()))));

        if (!exists) {
            wishlist.setId(wishlistStorage.size() + 1);
            wishlistStorage.add(wishlist);
            notifier.notifyObservers(wishlist, "added");
        }
        return wishlist;
    }

    public boolean removeFromWishlist(UUID userId, Long kosId) {
        return wishlistStorage.removeIf(w -> w.getUserId().equals(userId.toString()) &&
                w.getKosList().stream().anyMatch(k -> {
                    if (k.getId() == null)
                        return false;
                    UUID kosUuid = k.getId();
                    Long kosLongId = Math.abs(kosUuid.getMostSignificantBits()) % Long.MAX_VALUE;
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
                .filter(kos -> kos.getId() != null)
                .map(kos -> {
                    UUID kosUuid = kos.getId();
                    return Math.abs(kosUuid.getMostSignificantBits()) % Long.MAX_VALUE;
                })
                .collect(Collectors.toList());
    }

    public void toggleWishlist(UUID userId, UUID kosId) {
        Wishlist userWishlist = wishlistStorage.stream()
                .filter(w -> w.getUserId().equals(userId.toString()))
                .findFirst()
                .orElse(null);

        if (userWishlist == null) {
            userWishlist = new Wishlist();
            userWishlist.setUserId(userId.toString());
            userWishlist.setKosList(new ArrayList<>());
            userWishlist.setName("Wishlist " + userId);
            userWishlist.setId(wishlistStorage.size() + 1);
            wishlistStorage.add(userWishlist);
        }

        boolean exists = userWishlist.getKosList().stream()
                .anyMatch(k -> k.getId() != null && k.getId().equals(kosId));

        if (exists) {
            userWishlist.getKosList().removeIf(k -> k.getId() != null && k.getId().equals(kosId));
            notifier.notifyObservers(userWishlist, "removed");
        } else {
            Kos kos = new Kos();
            kos.setId(kosId);
            userWishlist.getKosList().add(kos);
            notifier.notifyObservers(userWishlist, "added");
        }
    }

    private boolean isValidWishlist(Wishlist wishlist) {

        return wishlist != null && wishlist.getName() != null && !wishlist.getName().trim().isEmpty();
    }

    private List<Kos> filterDuplicateKos(List<Kos> kosList) {
        if (kosList == null)
            return new ArrayList<>();

        List<Kos> uniqueKos = new ArrayList<>();
        for (Kos kos : kosList) {

            if (kos != null && !uniqueKos.contains(kos)) {
                uniqueKos.add(kos);
            }
        }
        return uniqueKos;
    }
}