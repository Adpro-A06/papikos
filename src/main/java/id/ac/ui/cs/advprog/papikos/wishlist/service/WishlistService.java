package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;

import java.util.List;
import java.util.UUID;

public interface WishlistService {
    
    void addToWishlist(UUID userId, UUID kosId);
    void removeFromWishlist(UUID userId, UUID kosId);
    boolean isInWishlist(UUID userId, UUID kosId);
    void clearUserWishlist(UUID userId);
    void toggleWishlist(UUID userId, UUID kosId);
    
    List<Kos> getUserWishlistItems(UUID userId);
    int getWishlistCount(UUID userId);
    
    // Add missing method for HomeController compatibility
    List<Long> getUserWishlistKosIdsAsLong(UUID userId);
}