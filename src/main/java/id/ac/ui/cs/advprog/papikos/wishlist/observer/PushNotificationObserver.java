package id.ac.ui.cs.advprog.papikos.wishlist.observer;

import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PushNotificationObserver implements WishlistObserver {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void update(Wishlist wishlist, String event) {
        try {
            // Buat notifikasi object
            NotificationMessage notification = createNotification(wishlist, event);
            String message = objectMapper.writeValueAsString(notification);

            // Kirim ke semua active sessions
            sessions.removeIf(session -> !session.isOpen());
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        System.err.println("Error sending notification: " + e.getMessage());
                    }
                }
            }

            // Log ke console juga untuk debugging
            System.out.println("🔔 Push Notification: " + notification.getMessage());

        } catch (Exception e) {
            System.err.println("Error creating push notification: " + e.getMessage());
        }
    }

    public void addSession(WebSocketSession session) {
        sessions.add(session);
        System.out.println("📱 New notification session added. Total active: " + sessions.size());
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
        System.out.println("📱 Notification session removed. Total active: " + sessions.size());
    }

    private NotificationMessage createNotification(Wishlist wishlist, String event) {
        String message;
        String type;
        
        switch (event.toLowerCase()) {
            case "added":
                message = "✅ Kos berhasil ditambahkan ke wishlist!";
                type = "success";
                break;
            case "removed":
                message = "🗑️ Kos dihapus dari wishlist";
                type = "info";
                break;
            case "cleared":
                message = "🧹 Wishlist telah dikosongkan";
                type = "warning";
                break;
            case "created":
                message = "📝 Wishlist baru telah dibuat";
                type = "info";
                break;
            default:
                message = "📋 Wishlist telah diperbarui";
                type = "info";
        }

        return new NotificationMessage(
            message,
            type,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            wishlist.getUserId(),
            event
        );
    }

    // Inner class untuk notifikasi
    public static class NotificationMessage {
        private String message;
        private String type;
        private String timestamp;
        private String userId;
        private String event;

        public NotificationMessage(String message, String type, String timestamp, String userId, String event) {
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
            this.userId = userId;
            this.event = event;
        }

        // Getters
        public String getMessage() { return message; }
        public String getType() { return type; }
        public String getTimestamp() { return timestamp; }
        public String getUserId() { return userId; }
        public String getEvent() { return event; }

        // Setterssss
        public void setMessage(String message) { this.message = message; }
        public void setType(String type) { this.type = type; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public void setUserId(String userId) { this.userId = userId; }
        public void setEvent(String event) { this.event = event; }


        
    }
}