package id.ac.ui.cs.advprog.papikos.chat.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    private WebSocketConfig webSocketConfig;

    @BeforeEach
    void setUp() {
        webSocketConfig = new WebSocketConfig();
    }

    @Test
    void testConfigureMessageBroker_ShouldEnableSimpleBrokerWithTopicPrefix() {
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);
        verify(messageBrokerRegistry, times(1)).enableSimpleBroker("/topic");
    }

    @Test
    void testConfigureMessageBroker_ShouldSetApplicationDestinationPrefix() {
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);
        verify(messageBrokerRegistry, times(1)).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testConfigureMessageBroker_ShouldCallBothMethods() {
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);
        verify(messageBrokerRegistry, times(1)).enableSimpleBroker("/topic");
        verify(messageBrokerRegistry, times(1)).setApplicationDestinationPrefixes("/app");
        verifyNoMoreInteractions(messageBrokerRegistry);
    }

    @Test
    void testWebSocketConfig_IsProperlyAnnotated() {
        assertTrue(webSocketConfig.getClass().isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class));

        assertTrue(webSocketConfig.getClass().isAnnotationPresent(
                org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker.class));

        assertTrue(org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer.class
                .isAssignableFrom(webSocketConfig.getClass()));
    }

    @Test
    void testConfigureMessageBroker_WithNullRegistry_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            webSocketConfig.configureMessageBroker(null);
        });
    }

    @Test
    void testRegisterStompEndpoints_WithNullRegistry_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            webSocketConfig.registerStompEndpoints(null);
        });
    }

    @Test
    void testWebSocketConfig_CanBeInstantiated() {
        WebSocketConfig config = new WebSocketConfig();
        assertNotNull(config);
        assertTrue(config instanceof org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer);
    }

    @Test
    void testConfigureMessageBroker_EnablesSimpleBrokerWithCorrectDestination() {
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);
        verify(messageBrokerRegistry).enableSimpleBroker(destinationCaptor.capture());
        assertEquals("/topic", destinationCaptor.getValue());
    }

    @Test
    void testConfigureMessageBroker_SetsCorrectApplicationDestinationPrefix() {
        ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes(prefixCaptor.capture());
        assertEquals("/app", prefixCaptor.getValue());
    }

    @Test
    void testConfigureMessageBroker_MethodExecutionOrder() {
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);
        verify(messageBrokerRegistry).enableSimpleBroker(anyString());
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes(anyString());
    }
}