package id.ac.ui.cs.advprog.papikos.wishlist.config;

import id.ac.ui.cs.advprog.papikos.wishlist.observer.PushNotificationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WishlistWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private PushNotificationObserver pushNotificationObserver;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WishlistNotificationWebSocketHandler(pushNotificationObserver), "/ws/wishlist-notifications")
                .setAllowedOrigins("*");
    }

    private static class WishlistNotificationWebSocketHandler implements WebSocketHandler {
        
        private final PushNotificationObserver pushNotificationObserver;

        public WishlistNotificationWebSocketHandler(PushNotificationObserver pushNotificationObserver) {
            this.pushNotificationObserver = pushNotificationObserver;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            pushNotificationObserver.addSession(session);
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            // Handle incoming messages if needed
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            System.err.println("WebSocket transport error: " + exception.getMessage());
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            pushNotificationObserver.removeSession(session);
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

        
    }
}