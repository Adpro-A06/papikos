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
        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry, times(1)).enableSimpleBroker("/topic");
    }

    @Test
    void testConfigureMessageBroker_ShouldSetApplicationDestinationPrefix() {
        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry, times(1)).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testConfigureMessageBroker_ShouldCallBothMethods() {
        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry, times(1)).enableSimpleBroker("/topic");
        verify(messageBrokerRegistry, times(1)).setApplicationDestinationPrefixes("/app");
        verifyNoMoreInteractions(messageBrokerRegistry);
    }

//    @Test
//    void testRegisterStompEndpoints_ShouldAddEndpointWithCorrectPath() {
//        // When
//        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);
//
//        // Then
//        verify(stompEndpointRegistry, times(1)).addEndpoint("/ws");
//    }
//
//    @Test
//    void testRegisterStompEndpoints_CallsAddEndpointOnce() {
//        // Given
//        ArgumentCaptor<String> endpointCaptor = ArgumentCaptor.forClass(String.class);
//
//        // When
//        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);
//
//        // Then
//        verify(stompEndpointRegistry).addEndpoint(endpointCaptor.capture());
//        assertEquals("/ws", endpointCaptor.getValue());
//    }

    @Test
    void testWebSocketConfig_IsProperlyAnnotated() {
        // Verify class has @Configuration annotation
        assertTrue(webSocketConfig.getClass().isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class));

        // Verify class has @EnableWebSocketMessageBroker annotation
        assertTrue(webSocketConfig.getClass().isAnnotationPresent(
                org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker.class));

        // Verify class implements WebSocketMessageBrokerConfigurer
        assertTrue(org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer.class
                .isAssignableFrom(webSocketConfig.getClass()));
    }

    @Test
    void testConfigureMessageBroker_WithNullRegistry_ShouldThrowException() {
        // Test edge case untuk null parameter
        assertThrows(NullPointerException.class, () -> {
            webSocketConfig.configureMessageBroker(null);
        });
    }

    @Test
    void testRegisterStompEndpoints_WithNullRegistry_ShouldThrowException() {
        // Test edge case untuk null parameter
        assertThrows(NullPointerException.class, () -> {
            webSocketConfig.registerStompEndpoints(null);
        });
    }

    @Test
    void testWebSocketConfig_CanBeInstantiated() {
        // Test constructor
        WebSocketConfig config = new WebSocketConfig();
        assertNotNull(config);
        assertTrue(config instanceof org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer);
    }

    @Test
    void testConfigureMessageBroker_EnablesSimpleBrokerWithCorrectDestination() {
        // Given
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);

        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry).enableSimpleBroker(destinationCaptor.capture());
        assertEquals("/topic", destinationCaptor.getValue());
    }

    @Test
    void testConfigureMessageBroker_SetsCorrectApplicationDestinationPrefix() {
        // Given
        ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);

        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes(prefixCaptor.capture());
        assertEquals("/app", prefixCaptor.getValue());
    }

    @Test
    void testConfigureMessageBroker_MethodExecutionOrder() {
        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then - verify both methods are called but don't check order since it doesn't matter
        verify(messageBrokerRegistry).enableSimpleBroker(anyString());
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes(anyString());
    }
}