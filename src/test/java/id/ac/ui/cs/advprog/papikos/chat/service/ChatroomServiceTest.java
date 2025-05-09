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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockChatroom = new Chatroom();
        mockChatroom.setId(1L);
        mockChatroom.setRenterId(101L);
        mockChatroom.setOwnerId(201L);
        mockChatroom.setPropertyId(301L);
        mockChatroom.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createChatroom_WhenNewChatroom_ShouldCreateAndReturnChatroom() {
        when(chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(101L, 201L, 301L))
                .thenReturn(Optional.empty());
        when(chatroomRepository.save(any(Chatroom.class)))
                .thenReturn(mockChatroom);

        Chatroom result = chatroomService.createChatroom(101L, 201L, 301L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(chatroomRepository)
                .findByRenterIdAndOwnerIdAndPropertyId(101L, 201L, 301L);
        verify(chatroomRepository).save(any(Chatroom.class));
    }

    @Test
    void createChatroom_WhenChatroomExists_ShouldReturnExistingChatroom() {
        when(chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(101L, 201L, 301L))
                .thenReturn(Optional.of(mockChatroom));

        Chatroom result = chatroomService.createChatroom(101L, 201L, 301L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(chatroomRepository)
                .findByRenterIdAndOwnerIdAndPropertyId(101L, 201L, 301L);
        verify(chatroomRepository, never()).save(any());
    }

    @Test
    void getChatroomsByRenterId_ShouldReturnList() {
        List<Chatroom> list = List.of(mockChatroom);
        when(chatroomRepository.findByRenterId(101L)).thenReturn(list);

        List<Chatroom> result = chatroomService.getChatroomsByRenterId(101L);

        assertEquals(1, result.size());
        assertSame(mockChatroom, result.get(0));
        verify(chatroomRepository).findByRenterId(101L);
        verify(chatroomRepository, never()).findByOwnerId(anyLong());
    }

    @Test
    void getChatroomsByOwnerId_ShouldReturnList() {
        List<Chatroom> list = List.of(mockChatroom);
        when(chatroomRepository.findByOwnerId(201L)).thenReturn(list);

        List<Chatroom> result = chatroomService.getChatroomsByOwnerId(201L);

        assertEquals(1, result.size());
        assertSame(mockChatroom, result.get(0));
        verify(chatroomRepository).findByOwnerId(201L);
        verify(chatroomRepository, never()).findByRenterId(anyLong());
    }

    @Test
    void getChatroomById_WhenExists_ShouldReturnChatroom() {
        when(chatroomRepository.findById(1L))
                .thenReturn(Optional.of(mockChatroom));

        Chatroom result = chatroomService.getChatroomById(1L);

        assertSame(mockChatroom, result);
        verify(chatroomRepository).findById(1L);
    }

    @Test
    void getChatroomById_WhenNotExists_ShouldThrow() {
        when(chatroomRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> chatroomService.getChatroomById(1L)
        );
        assertEquals("Chatroom not found with id: 1", ex.getMessage());
        verify(chatroomRepository).findById(1L);
    }
}