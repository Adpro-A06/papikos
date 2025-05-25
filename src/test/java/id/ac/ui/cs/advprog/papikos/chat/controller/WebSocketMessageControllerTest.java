package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.service.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class WebSocketMessageControllerTest {

    @Mock
    private MessageServiceImpl messageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketMessageController webSocketMessageController;

    private UUID chatroomId;
    private UUID senderId;
    private UUID messageId;
    private Message mockMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        chatroomId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        messageId = UUID.randomUUID();

        mockMessage = new Message();
        mockMessage.setId(messageId);
        mockMessage.setChatroomId(chatroomId);
        mockMessage.setSenderId(senderId);
        mockMessage.setContent("Test message");
        mockMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void sendMessage_ShouldProcessMessageAndBroadcast() {
        WebSocketMessageController.ChatMessage chatMessage = new WebSocketMessageController.ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setContent("Test message");

        when(messageService.sendMessage(chatroomId, senderId, "Test message"))
                .thenReturn(mockMessage);

        webSocketMessageController.sendMessage(chatroomId, chatMessage);

        verify(messageService).sendMessage(chatroomId, senderId, "Test message");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId),
                eq(mockMessage)
        );
    }

    @Test
    void sendMessage_ShouldHandleException() {
        WebSocketMessageController.ChatMessage chatMessage = new WebSocketMessageController.ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setContent("Test message");

        when(messageService.sendMessage(chatroomId, senderId, "Test message"))
                .thenThrow(new RuntimeException("Service error"));

        // Should not throw exception
        webSocketMessageController.sendMessage(chatroomId, chatMessage);

        verify(messageService).sendMessage(chatroomId, senderId, "Test message");
        // Should not broadcast if there's an error
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void broadcastMessageEdit_ShouldSendEditNotification() {
        webSocketMessageController.broadcastMessageEdit(chatroomId, mockMessage);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId + "/edit"),
                eq(mockMessage)
        );
    }

    @Test
    void broadcastMessageDelete_ShouldSendDeleteNotification() {
        webSocketMessageController.broadcastMessageDelete(chatroomId, messageId);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId + "/delete"),
                eq(messageId)
        );
    }

    @Test
    void chatMessage_ShouldSetAndGetProperties() {
        WebSocketMessageController.ChatMessage chatMessage = new WebSocketMessageController.ChatMessage();

        chatMessage.setSenderId(senderId);
        chatMessage.setContent("Test content");

        assertEquals(senderId, chatMessage.getSenderId());
        assertEquals("Test content", chatMessage.getContent());
    }
}