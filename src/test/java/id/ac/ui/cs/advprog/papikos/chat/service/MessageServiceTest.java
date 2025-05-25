package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @Mock
    private ChatroomServiceImpl chatroomService;

    @Mock
    private ChatCommandService chatCommandService;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Chatroom mockChatroom;
    private Message mockMessage;
    private UUID propertyId;
    private UUID chatroomId;
    private UUID renterId;
    private UUID senderId;
    private UUID messageId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        propertyId = UUID.randomUUID();
        chatroomId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        messageId = UUID.randomUUID();

        mockChatroom = new Chatroom();
        mockChatroom.setId(chatroomId);
        mockChatroom.setRenterId(renterId);
        mockChatroom.setOwnerId(UUID.randomUUID());
        mockChatroom.setPropertyId(propertyId);
        mockChatroom.setCreatedAt(LocalDateTime.now());

        mockMessage = new Message();
        mockMessage.setId(messageId);
        mockMessage.setChatroomId(chatroomId);
        mockMessage.setSenderId(senderId);
        mockMessage.setContent("hello");
        mockMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void getMessagesByChatroomId_ShouldReturnMessagesFromRepository() {
        List<Message> expectedMessages = Arrays.asList(mockMessage);
        when(messageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId))
                .thenReturn(expectedMessages);

        List<Message> result = messageService.getMessagesByChatroomId(chatroomId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockMessage, result.get(0));
        verify(messageRepository).findByChatroomIdOrderByTimestampDesc(chatroomId);
    }

    @Test
    void getMessagesByChatroomId_ShouldReturnEmptyList() {
        when(messageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId))
                .thenReturn(Collections.emptyList());

        List<Message> result = messageService.getMessagesByChatroomId(chatroomId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(messageRepository).findByChatroomIdOrderByTimestampDesc(chatroomId);
    }

    @Test
    void getMessagesByChatroomId_WithMultipleMessages_ShouldReturnAll() {
        Message message1 = new Message();
        message1.setId(UUID.randomUUID());
        message1.setContent("First message");

        Message message2 = new Message();
        message2.setId(UUID.randomUUID());
        message2.setContent("Second message");

        List<Message> expectedMessages = Arrays.asList(message1, message2);
        when(messageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId))
                .thenReturn(expectedMessages);

        List<Message> result = messageService.getMessagesByChatroomId(chatroomId);

        assertEquals(2, result.size());
        assertEquals(message1, result.get(0));
        assertEquals(message2, result.get(1));
    }

    @Test
    void sendMessage_ShouldFetchChatroom_DelegateToCommandService_AndReturnMessage() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.sendMessage(eq(mockChatroom), eq(senderId), eq("hello")))
                .thenReturn(mockMessage);

        Message result = messageService.sendMessage(chatroomId, senderId, "hello");

        assertNotNull(result);
        assertEquals(mockMessage.getId(), result.getId());
        assertEquals("hello", result.getContent());
        assertEquals(senderId, result.getSenderId());

        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).sendMessage(mockChatroom, senderId, "hello");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId),
                eq(mockMessage)
        );
    }

    @Test
    void sendMessage_WithEmptyContent_ShouldWork() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.sendMessage(eq(mockChatroom), eq(senderId), eq("")))
                .thenReturn(mockMessage);

        Message result = messageService.sendMessage(chatroomId, senderId, "");

        assertNotNull(result);
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).sendMessage(mockChatroom, senderId, "");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId),
                eq(mockMessage)
        );
    }

    @Test
    void sendMessage_WithNullContent_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            messageService.sendMessage(chatroomId, senderId, null);
        });

        assertEquals("Message content cannot be null", exception.getMessage());

        // Verify that no service calls were made after validation failure
        verify(chatroomService, never()).getChatroomById(any());
        verify(chatCommandService, never()).sendMessage(any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void editMessage_ShouldFetchChatroom_DelegateToCommandService_AndReturnMessage() {
        mockMessage.setContent("updated content");
        mockMessage.setEdited(true);

        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.editMessage(eq(mockChatroom), eq(messageId), eq("updated content")))
                .thenReturn(mockMessage);

        Message result = messageService.editMessage(chatroomId, messageId, "updated content");

        assertNotNull(result);
        assertEquals(mockMessage.getId(), result.getId());
        assertEquals("updated content", result.getContent());
        assertTrue(result.isEdited());

        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).editMessage(mockChatroom, messageId, "updated content");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId + "/edit"),
                eq(mockMessage)
        );
    }

    @Test
    void editMessage_WithNullContent_ShouldWork() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.editMessage(eq(mockChatroom), eq(messageId), eq(null)))
                .thenReturn(mockMessage);

        Message result = messageService.editMessage(chatroomId, messageId, null);

        assertNotNull(result);
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).editMessage(mockChatroom, messageId, null);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId + "/edit"),
                eq(mockMessage)
        );
    }

    @Test
    void deleteMessage_ShouldFetchChatroom_DelegateToCommandService_AndReturnResult() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.deleteMessage(eq(mockChatroom), eq(messageId)))
                .thenReturn(true);

        boolean result = messageService.deleteMessage(chatroomId, messageId);

        assertTrue(result);
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).deleteMessage(mockChatroom, messageId);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId + "/delete"),
                eq(messageId)
        );
    }

    @Test
    void deleteMessage_WhenUnsuccessful_ShouldNotBroadcast() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.deleteMessage(eq(mockChatroom), eq(messageId)))
                .thenReturn(false);

        boolean result = messageService.deleteMessage(chatroomId, messageId);

        assertFalse(result);
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).deleteMessage(mockChatroom, messageId);
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void undoLastAction_ShouldFetchChatroom_DelegateToCommandService_AndReturnResult() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.undoLastCommand(eq(mockChatroom), eq(messageId)))
                .thenReturn(true);

        boolean result = messageService.undoLastAction(chatroomId, messageId);

        assertTrue(result);
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).undoLastCommand(mockChatroom, messageId);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId + "/reload"),
                eq("Messages reloaded due to undo")
        );
    }

    @Test
    void undoLastAction_WhenUnsuccessful_ShouldNotBroadcast() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.undoLastCommand(eq(mockChatroom), eq(messageId)))
                .thenReturn(false);

        boolean result = messageService.undoLastAction(chatroomId, messageId);

        assertFalse(result);
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).undoLastCommand(mockChatroom, messageId);
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void sendMessage_WhenChatroomNotFound_ShouldThrowException() {
        when(chatroomService.getChatroomById(chatroomId))
                .thenThrow(new RuntimeException("Chatroom not found"));

        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(chatroomId, senderId, "hello");
        });

        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService, never()).sendMessage(any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void editMessage_WhenChatroomNotFound_ShouldThrowException() {
        when(chatroomService.getChatroomById(chatroomId))
                .thenThrow(new RuntimeException("Chatroom not found"));

        assertThrows(RuntimeException.class, () -> {
            messageService.editMessage(chatroomId, messageId, "updated");
        });

        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService, never()).editMessage(any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void deleteMessage_WhenChatroomNotFound_ShouldThrowException() {
        when(chatroomService.getChatroomById(chatroomId))
                .thenThrow(new RuntimeException("Chatroom not found"));

        assertThrows(RuntimeException.class, () -> {
            messageService.deleteMessage(chatroomId, messageId);
        });

        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService, never()).deleteMessage(any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void undoLastAction_WhenChatroomNotFound_ShouldThrowException() {
        when(chatroomService.getChatroomById(chatroomId))
                .thenThrow(new RuntimeException("Chatroom not found"));

        assertThrows(RuntimeException.class, () -> {
            messageService.undoLastAction(chatroomId, messageId);
        });

        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService, never()).undoLastCommand(any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void editMessage_WhenCommandServiceReturnsNull_ShouldHandleGracefully() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.editMessage(eq(mockChatroom), eq(messageId), eq("updated")))
                .thenReturn(null);

        Message result = messageService.editMessage(chatroomId, messageId, "updated");

        assertNull(result);
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).editMessage(mockChatroom, messageId, "updated");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId + "/edit"),
                (Object) eq(null)
        );
    }
}