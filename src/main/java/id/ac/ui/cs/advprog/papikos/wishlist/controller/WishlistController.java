package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthService authService;

    @GetMapping
    public String wishlistPage(Model model, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            if (user == null) {
                return "redirect:/api/auth/login";
            }

            List<Wishlist> userWishlists = wishlistService.getUserWishlist(user.getId());
            int wishlistCount = wishlistService.getWishlistCount(user.getId());
            List<Long> kosIds = wishlistService.getUserWishlistKosIdsAsLong(user.getId());

            List<id.ac.ui.cs.advprog.papikos.kos.model.Kos> wishlistItems = new java.util.ArrayList<>();
            for (Wishlist wishlist : userWishlists) {
                if (wishlist.getKosList() != null) {
                    wishlistItems.addAll(wishlist.getKosList());
                }
            }

            model.addAttribute("user", user);
            model.addAttribute("wishlists", userWishlists);
            model.addAttribute("wishlistCount", wishlistCount);
            model.addAttribute("kosIds", kosIds);
            model.addAttribute("hasWishlists", !userWishlists.isEmpty());
            model.addAttribute("wishlistItems", wishlistItems);

            return "wishlist/wishlistpage";

        } catch (Exception e) {
            model.addAttribute("error", "Error loading wishlist: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/page")
    public String wishlistPageAlternative(Model model, HttpSession session) {
        return wishlistPage(model, session);
    }

    @GetMapping("/toggle/{kosId}")
    public String toggleWishlist(@PathVariable UUID kosId, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session);
        if (user == null) {
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu.");
            return "redirect:/api/auth/login";
        }
        wishlistService.toggleWishlist(user.getId(), kosId);
        return "redirect:/penyewa/home";
    }

    @PostMapping("/clear")
    public String clearWishlist(HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session);
        if (user == null) {
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu.");
            return "redirect:/api/auth/login";
        }
        wishlistService.clearUserWishlist(user.getId());
        ra.addFlashAttribute("success", "Wishlist berhasil dikosongkan.");
        return "redirect:/wishlist";
    }

    private User getCurrentUser(HttpSession session) {
        try {
            String token = (String) session.getAttribute("JWT_TOKEN");
            if (token == null) {
                return null;
            }
            String idStr = authService.decodeToken(token);
            return authService.findById(UUID.fromString(idStr));
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            return null;
        }
    }
}