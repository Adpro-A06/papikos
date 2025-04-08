package id.ac.ui.cs.advprog.papikos.wishlist;

import java.util.ArrayList;
import java.util.List;

public class wishlistService {

    private List<Wishlist> wishlistStorage = new ArrayList<>();

    public Wishlist createWishlist(Wishlist wishlist) {
        
        if (wishlist.getName() == null || wishlist.getName().trim().isEmpty()) {
            return null;
        }
        
        wishlist.setId("W" + (wishlistStorage.size() + 1));
        
        
        List kosUniq = new ArrayList<>();
        for (Object item : wishlist.getKosList()) {
            
            if (!kosUniq.contains(item)) {
                kosUniq.add(item);
            }
        }
        wishlist.setKosList(kosUniq);
        
        wishlistStorage.add(wishlist);
        return wishlist;
    }
}
