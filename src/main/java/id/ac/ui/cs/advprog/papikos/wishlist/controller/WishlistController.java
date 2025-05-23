package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlists")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    public ResponseEntity<Wishlist> createWishlist(@RequestBody Wishlist wishlist) {
        Wishlist created = wishlistService.createWishlist(wishlist);
        if (created == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(created);
    }
}
