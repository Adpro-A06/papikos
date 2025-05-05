package id.ac.ui.cs.advprog.papikos.wishlist.repository;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import java.util.*;
import java.util.stream.Collectors;

public class WishlistRepository {

    private final Map<Integer, Wishlist> wishlists = new HashMap<>();
    private int idCounter = 1;

    public Wishlist save(Wishlist wishlist) {
        if (wishlist.getId() == null) {
            wishlist.setId(idCounter++);
        }
        wishlists.put(wishlist.getId(), wishlist);
        return wishlist;
    }
    

    public Optional<Wishlist> findById(int id) {
        return Optional.ofNullable(wishlists.get(id));
    }

    public List<Wishlist> findAll() {
        return new ArrayList<>(wishlists.values());
    }

    public boolean deleteById(int id) {
        return wishlists.remove(id) != null;
    }

    public void delete(Wishlist wishlist) {
        wishlists.values().removeIf(w -> 
            Objects.equals(w.getId(), wishlist.getId())
        );
    }

    public List<Wishlist> findByUserId(String userId) {
        return wishlists.values().stream()
            .filter(w -> Objects.equals(w.getUserId(), userId))
            .collect(Collectors.toList());
    }

    public Optional<Wishlist> findByUserIdAndKosId(String userId, String kosId) {
        return wishlists.values().stream()
            .filter(w -> Objects.equals(w.getUserId(), userId) &&
                         Objects.equals(w.getKosId(), kosId))
            .findFirst();
    }

    public void clear() {
        wishlists.clear();
        idCounter = 1;
    }
}
