package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.repository.ChatroomRepository;
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
    private ChatroomRepository chatroomRepository;

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
        when(chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId))
                .thenReturn(Optional.empty());

        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(mockChatroom);

        Chatroom result = chatroomService.createChatroom(renterId, ownerId, propertyId);

        assertNotNull(result);
        assertEquals(chatroomId, result.getId());
        assertEquals(renterId, result.getRenterId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(propertyId, result.getPropertyId());

        verify(chatroomRepository).save(any(Chatroom.class));
    }


    @Test
    void createChatroom_WhenChatroomExists_ShouldReturnExistingChatroom() {
        when(chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId))
                .thenReturn(Optional.of(mockChatroom));

        Chatroom result = chatroomService.createChatroom(renterId, ownerId, propertyId);

        assertNotNull(result);
        assertEquals(chatroomId, result.getId());
        assertEquals(renterId, result.getRenterId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(propertyId, result.getPropertyId());

        verify(chatroomRepository).findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);
        verify(chatroomRepository, never()).save(any());
    }


    @Test
    void createChatroom_WhenExistingChatroomFoundInIteration_ShouldReturnExisting() {
        Chatroom existingChatroom = new Chatroom();
        existingChatroom.setId(UUID.randomUUID());
        existingChatroom.setRenterId(renterId);
        existingChatroom.setOwnerId(ownerId);
        existingChatroom.setPropertyId(propertyId);

        when(chatroomRepository.findAll()).thenReturn(Collections.singletonList(existingChatroom));

        Chatroom result = chatroomService.createChatroom(renterId, ownerId, propertyId);

        assertNotNull(result);
        assertEquals(existingChatroom.getId(), result.getId());
        assertEquals(renterId, result.getRenterId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(propertyId, result.getPropertyId());

        verify(chatroomRepository).findAll();
        verify(chatroomRepository, never()).findByRenterIdAndOwnerIdAndPropertyId(any(), any(), any());
        verify(chatroomRepository, never()).save(any());
    }

    @Test
    void getChatroomsByRenterId_ShouldReturnList() {
        List<Chatroom> expectedChatrooms = Arrays.asList(mockChatroom);
        when(chatroomRepository.findByRenterIdForList(renterId)).thenReturn(expectedChatrooms);

        List<Chatroom> result = chatroomService.getChatroomsByRenterId(renterId);

        assertEquals(1, result.size());
        assertSame(mockChatroom, result.get(0));
        verify(chatroomRepository).findByRenterIdForList(renterId);
        verify(chatroomRepository, never()).findByOwnerIdForList(any());
    }

    @Test
    void getChatroomsByRenterId_ShouldReturnEmptyList() {
        when(chatroomRepository.findByRenterIdForList(renterId)).thenReturn(Collections.emptyList());

        List<Chatroom> result = chatroomService.getChatroomsByRenterId(renterId);

        assertEquals(0, result.size());
        verify(chatroomRepository).findByRenterIdForList(renterId);
    }

    @Test
    void getChatroomsByOwnerId_ShouldReturnList() {
        List<Chatroom> expectedChatrooms = Arrays.asList(mockChatroom);
        when(chatroomRepository.findByOwnerIdForList(ownerId)).thenReturn(expectedChatrooms);

        List<Chatroom> result = chatroomService.getChatroomsByOwnerId(ownerId);

        assertEquals(1, result.size());
        assertSame(mockChatroom, result.get(0));
        verify(chatroomRepository).findByOwnerIdForList(ownerId);
        verify(chatroomRepository, never()).findByRenterIdForList(any());
    }

    @Test
    void getChatroomsByOwnerId_ShouldReturnEmptyList() {
        when(chatroomRepository.findByOwnerIdForList(ownerId)).thenReturn(Collections.emptyList());

        List<Chatroom> result = chatroomService.getChatroomsByOwnerId(ownerId);

        assertEquals(0, result.size());
        verify(chatroomRepository).findByOwnerIdForList(ownerId);
    }

    @Test
    void getChatroomById_WhenExists_ShouldReturnChatroom() {
        when(chatroomRepository.findById(chatroomId)).thenReturn(Optional.of(mockChatroom));

        Chatroom result = chatroomService.getChatroomById(chatroomId);

        assertSame(mockChatroom, result);
        verify(chatroomRepository).findById(chatroomId);
    }

    @Test
    void getChatroomById_WhenNotExists_ShouldThrowRuntimeException() {
        when(chatroomRepository.findById(chatroomId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> chatroomService.getChatroomById(chatroomId)
        );

        assertEquals("Chatroom not found with id: " + chatroomId, exception.getMessage());
        verify(chatroomRepository).findById(chatroomId);
    }

    @Test
    void createChatroom_ShouldSetCreatedAtWhenSaving() {
        when(chatroomRepository.findAll()).thenReturn(Collections.emptyList());
        when(chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId))
                .thenReturn(Optional.empty());
        when(chatroomRepository.save(any(Chatroom.class))).thenAnswer(invocation -> {
            Chatroom chatroom = invocation.getArgument(0);
            assertNotNull(chatroom.getCreatedAt());
            chatroom.setId(chatroomId);
            return chatroom;
        });

        Chatroom result = chatroomService.createChatroom(renterId, ownerId, propertyId);

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertEquals(renterId, result.getRenterId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(propertyId, result.getPropertyId());
    }

    @Test
    void createChatroom_WithNullParameters_ShouldHandleGracefully() {
        when(chatroomRepository.findAll()).thenReturn(Collections.emptyList());
        when(chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(null, null, null))
                .thenReturn(Optional.empty());
        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(mockChatroom);

        Chatroom result = chatroomService.createChatroom(null, null, null);

        assertNotNull(result);
        verify(chatroomRepository).save(any(Chatroom.class));
    }

    @Test
    void getChatroomsByRenterId_WithMultipleChatrooms_ShouldReturnAll() {
        Chatroom chatroom1 = new Chatroom();
        chatroom1.setId(UUID.randomUUID());
        chatroom1.setRenterId(renterId);

        Chatroom chatroom2 = new Chatroom();
        chatroom2.setId(UUID.randomUUID());
        chatroom2.setRenterId(renterId);

        List<Chatroom> expectedChatrooms = Arrays.asList(chatroom1, chatroom2);
        when(chatroomRepository.findByRenterIdForList(renterId)).thenReturn(expectedChatrooms);

        List<Chatroom> result = chatroomService.getChatroomsByRenterId(renterId);

        assertEquals(2, result.size());
        assertTrue(result.contains(chatroom1));
        assertTrue(result.contains(chatroom2));
    }

    @Test
    void getChatroomsByOwnerId_WithMultipleChatrooms_ShouldReturnAll() {
        Chatroom chatroom1 = new Chatroom();
        chatroom1.setId(UUID.randomUUID());
        chatroom1.setOwnerId(ownerId);

        Chatroom chatroom2 = new Chatroom();
        chatroom2.setId(UUID.randomUUID());
        chatroom2.setOwnerId(ownerId);

        List<Chatroom> expectedChatrooms = Arrays.asList(chatroom1, chatroom2);
        when(chatroomRepository.findByOwnerIdForList(ownerId)).thenReturn(expectedChatrooms);

        List<Chatroom> result = chatroomService.getChatroomsByOwnerId(ownerId);

        assertEquals(2, result.size());
        assertTrue(result.contains(chatroom1));
        assertTrue(result.contains(chatroom2));
    }
}