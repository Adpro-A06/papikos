package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificationObserver implements WishlistObserver {

    @Override
    public void update(Wishlist wishlist, String event) {
        String emoji = getEventEmoji(event);
        System.out.println(emoji + " Console Notification: Wishlist '" + wishlist.getName() + "' has been " + event);
    }
    
    private String getEventEmoji(String event) {
        switch (event.toLowerCase()) {
            case "added": return "➕";
            case "removed": return "➖";
            case "cleared": return "🧹";
            case "created": return "📝";
            default: return "📋";
        }
    }
}