package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;
import java.util.Collections;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private static final Logger log = LoggerFactory.getLogger(WishlistController.class);

    private final WishlistService wishlistService;
    private final AuthService authService;

    @GetMapping
    public String showWishlist(
            HttpSession session, 
            Model model, 
            RedirectAttributes ra,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(required = false) String search) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("Unauthenticated user attempted to access wishlist");
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        try {
            log.info("Loading wishlist page for user: {}", user.getId());

            // Get all wishlist items
            List<Kos> allWishlistItems = wishlistService.getUserWishlistItems(user.getId());
            
            // Apply search filter if provided
            List<Kos> filteredItems = allWishlistItems;
            if (search != null && !search.trim().isEmpty()) {
                filteredItems = allWishlistItems.stream()
                    .filter(kos -> kos.getNama().toLowerCase().contains(search.toLowerCase()) ||
                                  kos.getAlamat().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
                log.info("Applied search filter '{}', found {} items", search, filteredItems.size());
            }

            // Apply sorting
            sortWishlistItems(filteredItems, sort);

            // Apply pagination
            int totalItems = filteredItems.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int start = page * size;
            int end = Math.min(start + size, totalItems);
            
            List<Kos> wishlistItems = start < totalItems ? 
                filteredItems.subList(start, end) : 
                Collections.emptyList();

            // Calculate stats from all items (not just current page)
            int wishlistCount = allWishlistItems.size();
            long availableCount = allWishlistItems.stream()
                    .filter(kos -> kos.getJumlah() > 0)
                    .count();
            
            double averagePrice = allWishlistItems.stream()
                    .filter(kos -> kos.getHarga() > 0)
                    .mapToInt(Kos::getHarga)
                    .average()
                    .orElse(0.0);

            // Add attributes to model
            model.addAttribute("user", user);
            model.addAttribute("wishlistItems", wishlistItems);
            model.addAttribute("wishlistCount", wishlistCount);
            model.addAttribute("availableCount", availableCount);
            model.addAttribute("averagePrice", averagePrice);
            model.addAttribute("hasWishlists", !allWishlistItems.isEmpty());
            
            // Pagination attributes
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalItems", totalItems);
            
            // Filter attributes
            model.addAttribute("sort", sort);
            model.addAttribute("search", search);

            log.info("Loaded {} wishlist items for user {} (page {}/{}, total: {})", 
                wishlistItems.size(), user.getId(), page + 1, totalPages, totalItems);
            
            // Debug log to check if items are being passed
            log.debug("Wishlist items being passed to view:");
            for (Kos kos : wishlistItems) {
                log.debug("- Kos: {} (ID: {}, Price: {})", kos.getNama(), kos.getId(), kos.getHarga());
            }

            // UBAH RETURN PATH INI - menambahkan folder "wishlist/"
            return "wishlist/wishlistpage";

        } catch (Exception e) {
            log.error("Error loading wishlist page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading wishlist: " + e.getMessage());
            model.addAttribute("wishlistItems", Collections.emptyList());
            model.addAttribute("wishlistCount", 0);
            model.addAttribute("availableCount", 0);
            model.addAttribute("averagePrice", 0.0);
            
            // UBAH RETURN PATH INI JUGA
            return "wishlist/wishlistpage";
        }
    }

    private void sortWishlistItems(List<Kos> items, String sort) {
        switch (sort) {
            case "oldest":
                // For oldest, keep original order
                break;
            case "price-low":
                items.sort(Comparator.comparing(Kos::getHarga));
                break;
            case "price-high":
                items.sort(Comparator.comparing(Kos::getHarga).reversed());
                break;
            case "name":
                items.sort(Comparator.comparing(Kos::getNama, 
                    Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            default: // newest
                Collections.reverse(items);
                break;
        }
        log.debug("Applied sort: {}", sort);
    }

    @PostMapping("/toggle/{kosId}")
    public String toggleWishlist(@PathVariable String kosId, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("Unauthenticated user attempted to toggle wishlist");
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        try {
            UUID kosUuid = UUID.fromString(kosId);
            log.info("Toggle wishlist request - User: {}, KosId: {}", user.getId(), kosUuid);

            wishlistService.toggleWishlist(user.getId(), kosUuid);
            
            // Check final state for success message
            boolean isNowInWishlist = wishlistService.isInWishlist(user.getId(), kosUuid);
            if (isNowInWishlist) {
                ra.addFlashAttribute("success", "Kos berhasil ditambahkan ke wishlist.");
                log.info("Kos {} added to wishlist for user {}", kosUuid, user.getId());
            } else {
                ra.addFlashAttribute("success", "Kos berhasil dihapus dari wishlist.");
                log.info("Kos {} removed from wishlist for user {}", kosUuid, user.getId());
            }

        } catch (IllegalArgumentException e) {
            log.warn("Validation error in toggleWishlist: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error in toggleWishlist: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", "Terjadi kesalahan: " + e.getMessage());
        }

        return "redirect:/wishlist";
    }

    @PostMapping("/clear")
    public String clearWishlist(HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("Unauthenticated user attempted to clear wishlist");
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }
        
        try {
            log.info("Clear wishlist request for user: {}", user.getId());
            
            int countBefore = wishlistService.getWishlistCount(user.getId());
            wishlistService.clearUserWishlist(user.getId());
            
            ra.addFlashAttribute("success", "Wishlist berhasil dikosongkan. " + countBefore + " item dihapus.");
            log.info("Cleared {} items from wishlist for user {}", countBefore, user.getId());
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error in clearWishlist: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error clearing wishlist: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", "Gagal mengosongkan wishlist: " + e.getMessage());
        }
        
        return "redirect:/wishlist";
    }

    private User getCurrentUser(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) {
            log.warn("No JWT token found in session");
            return null;
        }

        try {
            String idStr = authService.decodeToken(token);
            return authService.findById(UUID.fromString(idStr));
        } catch (Exception e) {
            log.error("Failed to decode JWT token: {}", e.getMessage());
            return null;
        }
    }
}