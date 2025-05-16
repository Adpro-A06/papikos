package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ChatWebSocketControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    private Message mockMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMessage = new Message();
        mockMessage.setId(1L);
        mockMessage.setChatroomId(1L);
        mockMessage.setSenderId(101L);
        mockMessage.setContent("Test message");
        mockMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void sendMessage_ShouldSaveAndBroadcast() {
        // Create payload for the test
        ChatMessageRequest request = new ChatMessageRequest();
        request.setSenderId(101L);
        request.setContent("Test message");

        when(messageService.sendMessage(anyLong(), anyLong(), anyString())).thenReturn(mockMessage);

        chatWebSocketController.sendMessage(1L, request);

        verify(messageService).sendMessage(eq(1L), eq(101L), eq("Test message"));
        verify(messagingTemplate).convertAndSend(eq("/topic/chatrooms/1"), any(Message.class));
    }

    @Test
    void editMessage_ShouldUpdateAndBroadcast() {
        // Create payload for the test
        ChatMessageRequest request = new ChatMessageRequest();
        request.setContent("Edited message");

        when(messageService.editMessage(anyLong(), anyLong(), anyString())).thenReturn(mockMessage);

        chatWebSocketController.editMessage(1L, 1L, request);

        verify(messageService).editMessage(eq(1L), eq(1L), eq("Edited message"));
        verify(messagingTemplate).convertAndSend(eq("/topic/chatrooms/1"), any(Message.class));
    }

    @Test
    void deleteMessage_ShouldDeleteAndBroadcast() {
        when(messageService.deleteMessage(anyLong(), anyLong())).thenReturn(true);

        chatWebSocketController.deleteMessage(1L, 1L);

        verify(messageService).deleteMessage(eq(1L), eq(1L));
        verify(messagingTemplate).convertAndSend(eq("/topic/chatrooms/1/delete"), eq(1L));
    }

    // Helper class to represent the WebSocket message payload
    static class ChatMessageRequest {
        private Long senderId;
        private String content;

        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}