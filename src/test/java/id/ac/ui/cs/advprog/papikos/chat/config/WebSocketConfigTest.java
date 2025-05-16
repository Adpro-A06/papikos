package id.ac.ui.cs.advprog.papikos.chat.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketEndpointRegistration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class WebSocketConfigTest {

    @Test
    void testWebSocketConfigExists() {
        WebSocketConfig config = new WebSocketConfig();
        assertNotNull(config);
    }

    @Test
    void testConfigureMessageBroker() {
        // Mock
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        // Create config and test
        WebSocketConfig config = new WebSocketConfig();
        config.configureMessageBroker(registry);

        // Verify expected method calls
        verify(registry).enableSimpleBroker("/topic");
        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testRegisterStompEndpoints() {
        // Mock
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        WebSocketEndpointRegistration registration = mock(WebSocketEndpointRegistration.class);

        // Setup chain of mocks
        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any())).thenReturn(registration);

        // Create config and test
        WebSocketConfig config = new WebSocketConfig();
        config.registerStompEndpoints(registry);

        // Verify expected method call
        verify(registry).addEndpoint("/ws");
    }
}