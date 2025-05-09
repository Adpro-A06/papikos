package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @Mock
    private ChatroomService chatroomService;

    @Mock
    private ChatCommandService chatCommandService;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Chatroom mockChatroom;
    private Message mockMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // sample Chatroom
        mockChatroom = new Chatroom();
        mockChatroom.setId(1L);
        mockChatroom.setRenterId(101L);
        mockChatroom.setOwnerId(201L);
        mockChatroom.setPropertyId(301L);
        mockChatroom.setCreatedAt(LocalDateTime.now());

        // sample Message
        mockMessage = new Message();
        mockMessage.setId(11L);
        mockMessage.setChatroomId(mockChatroom.getId());
        mockMessage.setSenderId(101L);
        mockMessage.setContent("hello");
        mockMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void sendMessage_ShouldFetchChatroom_DelegateToCommandService_AndReturnMessage() {
        // Arrange
        when(chatroomService.getChatroomById(1L)).thenReturn(mockChatroom);
        when(chatCommandService.sendMessage(eq(mockChatroom), eq(101L), eq("hello")))
                .thenReturn(mockMessage);

        // Act
        Message result = messageService.sendMessage(1L, 101L, "hello");

        // Assert
        assertNotNull(result);
        assertEquals(mockMessage.getId(), result.getId());
        verify(chatroomService).getChatroomById(1L);
        verify(chatCommandService).sendMessage(mockChatroom, 101L, "hello");
    }

    @Test
    void editMessage_ShouldFetchChatroom_DelegateToCommandService_AndReturnMessage() {
        // Arrange
        when(chatroomService.getChatroomById(1L)).thenReturn(mockChatroom);
        when(chatCommandService.editMessage(eq(mockChatroom), eq(11L), eq("upd")))
                .thenReturn(mockMessage);

        // Act
        Message result = messageService.editMessage(1L, 11L, "upd");

        // Assert
        assertNotNull(result);
        assertEquals(mockMessage.getId(), result.getId());
        verify(chatroomService).getChatroomById(1L);
        verify(chatCommandService).editMessage(mockChatroom, 11L, "upd");
    }

    @Test
    void deleteMessage_ShouldFetchChatroom_DelegateToCommandService_AndReturnResult() {
        // Arrange
        when(chatroomService.getChatroomById(1L)).thenReturn(mockChatroom);
        when(chatCommandService.deleteMessage(eq(mockChatroom), eq(11L)))
                .thenReturn(true);

        // Act
        boolean result = messageService.deleteMessage(1L, 11L);

        // Assert
        assertTrue(result);
        verify(chatroomService).getChatroomById(1L);
        verify(chatCommandService).deleteMessage(mockChatroom, 11L);
    }

    @Test
    void undoLastAction_ShouldFetchChatroom_DelegateToCommandService_AndReturnResult() {
        // Arrange
        when(chatroomService.getChatroomById(1L)).thenReturn(mockChatroom);
        when(chatCommandService.undoLastCommand(eq(mockChatroom), eq(11L)))
                .thenReturn(true);

        // Act
        boolean result = messageService.undoLastAction(1L, 11L);

        // Assert
        assertTrue(result);
        verify(chatroomService).getChatroomById(1L);
        verify(chatCommandService).undoLastCommand(mockChatroom, 11L);
    }
}