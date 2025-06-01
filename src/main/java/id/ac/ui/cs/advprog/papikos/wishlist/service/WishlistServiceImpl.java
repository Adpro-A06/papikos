package id.ac.ui.cs.advprog.papikos.wishlist.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistSubject;
import id.ac.ui.cs.advprog.papikos.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final KosRepository kosRepository;
    private final UserRepository userRepository;
    private final WishlistSubject wishlistSubject;
    
    // In-memory storage like the original approach
    private final List<Wishlist> wishlistStorage = new ArrayList<>();

    @Override
    public void addToWishlist(UUID userId, UUID kosId) {
        log.info("Starting add to wishlist operation for user {} with kos {}", userId, kosId);

        try {
            validateUserExists(userId);
            validateKosExists(kosId);

            if (isInWishlist(userId, kosId)) {
                log.warn("Kos {} already in wishlist for user {}", kosId, userId);
                return;
            }

            // Find or create user wishlist
            Wishlist userWishlist = getUserWishlistEntity(userId);
            if (userWishlist == null) {
                userWishlist = new Wishlist("My Wishlist", userId.toString());
                userWishlist.setId(wishlistStorage.size() + 1);
                userWishlist.setKosList(new ArrayList<>());
                wishlistStorage.add(userWishlist);
                log.info("Created new wishlist for user {}", userId);
            }

            // Get kos and add to wishlist
            Kos kos = kosRepository.findById(kosId).orElse(null);
            if (kos != null) {
                userWishlist.getKosList().add(kos);
                log.info("Successfully added kos {} to wishlist for user {}", kosId, userId);
                
                // Notify observers
                wishlistSubject.notifyObservers(userWishlist, "added");
            } else {
                throw new IllegalArgumentException("Kos tidak ditemukan");
            }

        } catch (Exception e) {
            log.error("Failed to add kos {} to wishlist for user {}: {}", kosId, userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void removeFromWishlist(UUID userId, UUID kosId) {
        log.info("Starting remove from wishlist operation for user {} with kos {}", userId, kosId);

        try {
            validateUserExists(userId);

            Wishlist userWishlist = getUserWishlistEntity(userId);
            if (userWishlist != null) {
                boolean removed = userWishlist.getKosList().removeIf(kos -> kos.getId().equals(kosId));
                
                if (removed) {
                    log.info("Successfully removed kos {} from wishlist for user {}", kosId, userId);
                    
                    // Notify observers
                    wishlistSubject.notifyObservers(userWishlist, "removed");
                } else {
                    log.warn("Kos {} not found in wishlist for user {}", kosId, userId);
                }
            } else {
                log.warn("No wishlist found for user {}", userId);
            }

        } catch (Exception e) {
            log.error("Failed to remove kos {} from wishlist for user {}: {}", kosId, userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isInWishlist(UUID userId, UUID kosId) {
        log.debug("Checking if kos {} is in wishlist for user {}", kosId, userId);

        try {
            Wishlist userWishlist = getUserWishlistEntity(userId);
            if (userWishlist != null && userWishlist.getKosList() != null) {
                boolean found = userWishlist.getKosList().stream()
                        .anyMatch(kos -> kos.getId().equals(kosId));
                
                log.debug("Kos {} in wishlist for user {}: {}", kosId, userId, found);
                return found;
            }
            
            return false;

        } catch (Exception e) {
            log.error("Error checking wishlist for user {} and kos {}: {}", userId, kosId, e.getMessage());
            return false;
        }
    }

    @Override
    public void clearUserWishlist(UUID userId) {
        log.info("Starting clear wishlist operation for user {}", userId);

        try {
            validateUserExists(userId);

            Wishlist userWishlist = getUserWishlistEntity(userId);
            if (userWishlist != null) {
                int itemCount = userWishlist.getKosList().size();
                userWishlist.getKosList().clear();
                
                log.info("Cleared {} items from wishlist for user {}", itemCount, userId);
                
                // Notify observers
                wishlistSubject.notifyObservers(userWishlist, "cleared");
            } else {
                log.warn("No wishlist found for user {}", userId);
            }

        } catch (Exception e) {
            log.error("Failed to clear wishlist for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void toggleWishlist(UUID userId, UUID kosId) {
        log.info("Toggling wishlist for user {} with kos {}", userId, kosId);

        try {
            if (isInWishlist(userId, kosId)) {
                removeFromWishlist(userId, kosId);
                log.info("Toggled OFF: User={}, Kos={}", userId, kosId);
            } else {
                addToWishlist(userId, kosId);
                log.info("Toggled ON: User={}, Kos={}", userId, kosId);
            }
        } catch (Exception e) {
            log.error("Failed to toggle wishlist for user {} with kos {}: {}", userId, kosId, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Kos> getUserWishlistItems(UUID userId) {
        log.info("Getting wishlist items for user {}", userId);

        try {
            validateUserExists(userId);

            Wishlist userWishlist = getUserWishlistEntity(userId);
            if (userWishlist != null && userWishlist.getKosList() != null) {
                List<Kos> kosItems = new ArrayList<>(userWishlist.getKosList());
                
                log.info("Found {} kos items in wishlist for user {}", kosItems.size(), userId);
                
                for (int i = 0; i < kosItems.size(); i++) {
                    Kos kos = kosItems.get(i);
                    log.debug("Wishlist item {}: ID={}, Name={}, Price={}, Available={}", 
                        i + 1, kos.getId(), kos.getNama(), kos.getHarga(), kos.getJumlah());
                }
                
                return kosItems;
            } else {
                log.warn("No wishlist found for user {} or wishlist is empty", userId);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            log.error("Error getting wishlist items for user {}: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public int getWishlistCount(UUID userId) {
        log.debug("Getting wishlist count for user {}", userId);

        try {
            Wishlist userWishlist = getUserWishlistEntity(userId);
            if (userWishlist != null && userWishlist.getKosList() != null) {
                int count = userWishlist.getKosList().size();
                log.debug("Wishlist count for user {}: {}", userId, count);
                return count;
            }

            log.debug("No wishlist found for user {}, returning count 0", userId);
            return 0;

        } catch (Exception e) {
            log.error("Error getting wishlist count for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Long> getUserWishlistKosIdsAsLong(UUID userId) {
        log.debug("Getting wishlist kos IDs as Long for user {}", userId);

        try {
            Wishlist userWishlist = getUserWishlistEntity(userId);
            if (userWishlist != null && userWishlist.getKosList() != null) {
                List<Long> longIds = userWishlist.getKosList().stream()
                        .filter(kos -> kos.getId() != null)
                        .map(kos -> {
                            try {
                                return Math.abs(kos.getId().hashCode()) % Long.MAX_VALUE;
                            } catch (Exception e) {
                                log.warn("Error converting UUID to Long: {}", kos.getId());
                                return null;
                            }
                        })
                        .filter(id -> id != null)
                        .collect(Collectors.toList());

                log.debug("Found {} kos IDs for user {}", longIds.size(), userId);
                return longIds;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            log.error("Error getting wishlist kos IDs for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    // Helper methods
    private Wishlist getUserWishlistEntity(UUID userId) {
        Wishlist wishlist = wishlistStorage.stream()
                .filter(w -> w.getUserId().equals(userId.toString()))
                .findFirst()
                .orElse(null);
                
        if (wishlist != null) {
            log.debug("Found existing wishlist for user {} with {} items", userId, 
                wishlist.getKosList() != null ? wishlist.getKosList().size() : 0);
        } else {
            log.debug("No wishlist found for user {}", userId);
        }
        
        return wishlist;
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User dengan ID " + userId + " tidak ditemukan");
        }
    }

    private void validateKosExists(UUID kosId) {
        if (!kosRepository.existsById(kosId)) {
            throw new IllegalArgumentException("Kos dengan ID " + kosId + " tidak ditemukan");
        }
    }
}