package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
                return "redirect:/api/auth/login"; // Redirect ke login page sesuai dengan AuthController
            }

            // Get user's wishlist data
            List<Wishlist> userWishlists = wishlistService.getUserWishlist(user.getId());
            int wishlistCount = wishlistService.getWishlistCount(user.getId());
            List<Long> kosIds = wishlistService.getUserWishlistKosIdsAsLong(user.getId());

            // Add data to model for the HTML template
            model.addAttribute("user", user);
            model.addAttribute("wishlists", userWishlists);
            model.addAttribute("wishlistCount", wishlistCount);
            model.addAttribute("kosIds", kosIds);
            model.addAttribute("hasWishlists", !userWishlists.isEmpty());
            
            return "wishlist/wishlistpage"; // Path ke template HTML
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading wishlist: " + e.getMessage());
            return "error"; // Return error page
        }
    }

    @GetMapping("/page")
    public String wishlistPageAlternative(Model model, HttpSession session) {
        return wishlistPage(model, session);
    }

    private User getCurrentUser(HttpSession session) {
        try {
            // Ambil JWT token dari session (sesuai dengan AuthController)
            String token = (String) session.getAttribute("JWT_TOKEN");
            if (token == null) {
                return null;
            }

            // Decode token untuk mendapatkan user ID
            String idStr = authService.decodeToken(token);
            return authService.findById(UUID.fromString(idStr));
            
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            return null;
        }
    }
}