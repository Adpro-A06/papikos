package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.repository.ChatroomRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatroomServiceTest {

    @Mock
    private ChatroomRepositoryImpl chatroomRepository;

    @InjectMocks
    private ChatroomServiceImpl chatroomService;

    private Chatroom mockChatroom;
    private UUID propertyId;
    private UUID renterId;
    private UUID ownerId;
    private UUID chatroomId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        propertyId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        chatroomId = UUID.randomUUID();

        mockChatroom = new Chatroom();
        mockChatroom.setId(chatroomId);
        mockChatroom.setRenterId(renterId);
        mockChatroom.setOwnerId(ownerId);
        mockChatroom.setPropertyId(propertyId);
        mockChatroom.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createChatroom_WhenNewChatroom_ShouldCreateAndReturnChatroom() {
        when(chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId))
                .thenReturn(Optional.empty());
        when(chatroomRepository.save(any(Chatroom.class)))
                .thenReturn(mockChatroom);

        Chatroom result = chatroomService.createChatroom(renterId, ownerId, propertyId);

        assertNotNull(result);
        assertEquals(chatroomId, result.getId());
        verify(chatroomRepository)
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);
        verify(chatroomRepository).save(any(Chatroom.class));
    }

    @Test
    void createChatroom_WhenChatroomExists_ShouldReturnExistingChatroom() {
        when(chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId))
                .thenReturn(Optional.of(mockChatroom));

        Chatroom result = chatroomService.createChatroom(renterId, ownerId, propertyId);

        assertNotNull(result);
        assertEquals(chatroomId, result.getId());
        verify(chatroomRepository)
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);
        verify(chatroomRepository, never()).save(any());
    }

    @Test
    void getChatroomsByRenterId_ShouldReturnList() {
        List<Chatroom> list = List.of(mockChatroom);
        when(chatroomRepository.findByRenterId(renterId)).thenReturn(list);

        List<Chatroom> result = chatroomService.getChatroomsByRenterId(renterId);

        assertEquals(1, result.size());
        assertSame(mockChatroom, result.get(0));
        verify(chatroomRepository).findByRenterId(renterId);
        verify(chatroomRepository, never()).findByOwnerId(any());
    }

    @Test
    void getChatroomsByOwnerId_ShouldReturnList() {
        List<Chatroom> list = List.of(mockChatroom);
        when(chatroomRepository.findByOwnerId(ownerId)).thenReturn(list);

        List<Chatroom> result = chatroomService.getChatroomsByOwnerId(ownerId);

        assertEquals(1, result.size());
        assertSame(mockChatroom, result.get(0));
        verify(chatroomRepository).findByOwnerId(ownerId);
        verify(chatroomRepository, never()).findByRenterId(any());
    }

    @Test
    void getChatroomById_WhenExists_ShouldReturnChatroom() {
        when(chatroomRepository.findById(chatroomId))
                .thenReturn(Optional.of(mockChatroom));

        Chatroom result = chatroomService.getChatroomById(chatroomId);

        assertSame(mockChatroom, result);
        verify(chatroomRepository).findById(chatroomId);
    }

    @Test
    void getChatroomById_WhenNotExists_ShouldThrow() {
        when(chatroomRepository.findById(chatroomId))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> chatroomService.getChatroomById(chatroomId)
        );
        assertEquals("Chatroom not found with id: " + chatroomId, ex.getMessage());
        verify(chatroomRepository).findById(chatroomId);
    }
}
