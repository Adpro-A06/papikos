package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.ChatroomRepository;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatroomServiceTest {

    @Mock
    private ChatroomRepository chatroomRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatCommandService chatCommandService;

    @InjectMocks
    private ChatroomServiceImpl chatroomService;

    private Chatroom mockChatroom;
    private Message mockMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        mockChatroom = new Chatroom();
        mockChatroom.setId(1L);
        mockChatroom.setRenterId(101L);
        mockChatroom.setOwnerId(201L);
        mockChatroom.setPropertyId(301L);
        mockChatroom.setCreatedAt(LocalDateTime.now());

        mockMessage = new Message();
        mockMessage.setId(1L);
        mockMessage.setChatroomId(1L);
        mockMessage.setSenderId(101L);
        mockMessage.setContent("Test message");
        mockMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void createChatroom_WhenNewChatroom_ShouldCreateAndReturnChatroom() {
        // Arrange
        when(chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(mockChatroom);

        // Act
        Chatroom result = chatroomService.createChatroom(101L, 201L, 301L);

        // Assert
        assertNotNull(result);
        assertEquals(mockChatroom.getId(), result.getId());
        verify(chatroomRepository).findByRenterIdAndOwnerIdAndPropertyId(101L, 201L, 301L);
        verify(chatroomRepository).save(any(Chatroom.class));
    }

    @Test
    void createChatroom_WhenChatroomExists_ShouldReturnExistingChatroom() {
        // Arrange
        when(chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(mockChatroom));

        // Act
        Chatroom result = chatroomService.createChatroom(101L, 201L, 301L);

        // Assert
        assertNotNull(result);
        assertEquals(mockChatroom.getId(), result.getId());
        verify(chatroomRepository).findByRenterIdAndOwnerIdAndPropertyId(101L, 201L, 301L);
        verify(chatroomRepository, never()).save(any(Chatroom.class));
    }

    @Test
    void getChatroomsByUserId_WhenUserIsRenter_ShouldReturnRenterChatrooms() {
        // Arrange
        List<Chatroom> chatrooms = new ArrayList<>();
        chatrooms.add(mockChatroom);
        when(chatroomRepository.findByRenterId(anyLong())).thenReturn(chatrooms);

        // Act
        List<Chatroom> result = chatroomService.getChatroomsByRenterId(101L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockChatroom.getId(), result.get(0).getId());
        verify(chatroomRepository).findByRenterId(101L);
        verify(chatroomRepository, never()).findByOwnerId(anyLong());
    }

    @Test
    void getChatroomsByUserId_WhenUserIsOwner_ShouldReturnOwnerChatrooms() {
        List<Chatroom> chatrooms = new ArrayList<>();
        chatrooms.add(mockChatroom);
        when(chatroomRepository.findByOwnerId(anyLong())).thenReturn(chatrooms);

        List<Chatroom> result = chatroomService.getChatroomsByOwnerId(201L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockChatroom.getId(), result.get(0).getId());
        verify(chatroomRepository).findByOwnerId(201L);
        verify(chatroomRepository, never()).findByRenterId(anyLong());
    }

    @Test
    void getChatroomById_WhenChatroomExists_ShouldReturnChatroom() {
        // Arrange
        when(chatroomRepository.findById(anyLong())).thenReturn(Optional.of(mockChatroom));

        // Act
        Chatroom result = chatroomService.getChatroomById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(mockChatroom.getId(), result.getId());
        verify(chatroomRepository).findById(1L);
    }

    @Test
    void getChatroomById_WhenChatroomDoesNotExist_ShouldThrowException() {
        // Arrange
        when(chatroomRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            chatroomService.getChatroomById(1L);
        });

        assertEquals("Chatroom not found with id: 1", exception.getMessage());
        verify(chatroomRepository).findById(1L);
    }

    @Test
    void sendMessage_ShouldUseCommandServiceToSendMessage() {
        // Arrange
        when(chatroomRepository.findById(anyLong())).thenReturn(Optional.of(mockChatroom));
        when(chatCommandService.sendMessage(any(Chatroom.class), anyLong(), anyString())).thenReturn(mockMessage);
        Chatroom chatroom = chatroomService.getChatroomById(1L);
        // Act
        Message result = chatCommandService.sendMessage(chatroom, 101L, "Test message");

        // Assert
        assertNotNull(result);
        assertEquals(mockMessage.getId(), result.getId());
        verify(chatroomRepository).findById(1L);
        verify(chatCommandService).sendMessage(mockChatroom, 101L, "Test message");
    }

    @Test
    void editMessage_ShouldUseCommandServiceToEditMessage() {
        // Arrange
        when(chatroomRepository.findById(anyLong())).thenReturn(Optional.of(mockChatroom));
        when(chatCommandService.editMessage(any(Chatroom.class), anyLong(), anyString())).thenReturn(mockMessage);
        Chatroom chatroom = chatroomService.getChatroomById(1L);

        // Act
        Message result = chatCommandService.editMessage(chatroom, 1L, "Updated message");

        // Assert
        assertNotNull(result);
        assertEquals(mockMessage.getId(), result.getId());
        verify(chatroomRepository).findById(1L);
        verify(chatCommandService).editMessage(mockChatroom, 1L, "Updated message");
    }

    @Test
    void deleteMessage_ShouldUseCommandServiceToDeleteMessage() {
        // Arrange
        when(chatroomRepository.findById(anyLong())).thenReturn(Optional.of(mockChatroom));
        when(chatCommandService.deleteMessage(any(Chatroom.class), anyLong())).thenReturn(true);
        Chatroom chatroom = chatroomService.getChatroomById(1L);

        // Act
        boolean result = chatCommandService.deleteMessage(chatroom, 1L);

        // Assert
        assertTrue(result);
        verify(chatroomRepository).findById(1L);
        verify(chatCommandService).deleteMessage(mockChatroom, 1L);
    }

    @Test
    void undoLastAction_ShouldUseCommandServiceToUndoAction() {
        // Arrange
        Chatroom chatroom = chatroomService.getChatroomById(1L);
        when(chatCommandService.undoLastCommand(chatroom, mockMessage.getId())).thenReturn(true);

        // Act
        boolean result = chatCommandService.undoLastCommand(chatroom, mockMessage.getId());

        // Assert
        assertTrue(result);
        verify(chatCommandService).undoLastCommand(chatroom, mockMessage.getId());
    }
}