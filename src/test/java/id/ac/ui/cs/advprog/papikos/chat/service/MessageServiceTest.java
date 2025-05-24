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
    void sendMessage_ShouldFetchChatroom_DelegateToCommandService_AndReturnMessage() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.sendMessage(eq(mockChatroom), eq(senderId), eq("hello")))
                .thenReturn(mockMessage);

        Message result = messageService.sendMessage(chatroomId, senderId, "hello");

        assertNotNull(result);
        assertEquals(mockMessage.getId(), result.getId());
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).sendMessage(mockChatroom, senderId, "hello");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chatroom/" + chatroomId),
                eq(mockMessage)
        );
    }

    @Test
    void editMessage_ShouldFetchChatroom_DelegateToCommandService_AndReturnMessage() {
        when(chatroomService.getChatroomById(chatroomId)).thenReturn(mockChatroom);
        when(chatCommandService.editMessage(eq(mockChatroom), eq(messageId), eq("upd")))
                .thenReturn(mockMessage);

        Message result = messageService.editMessage(chatroomId, messageId, "upd");

        assertNotNull(result);
        assertEquals(mockMessage.getId(), result.getId());
        verify(chatroomService).getChatroomById(chatroomId);
        verify(chatCommandService).editMessage(mockChatroom, messageId, "upd");
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
}