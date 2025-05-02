package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.repository.ChatroomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChatroomServiceTest {

    private ChatroomService chatroomService;
    private ChatroomRepository chatroomRepository;

    private Chatroom chatroom;

    @BeforeEach
    void setUp() {
        chatroomRepository = mock(ChatroomRepository.class);
        chatroomService = new ChatroomService(chatroomRepository);

        chatroom = new Chatroom();
        chatroom.setId(1L);
        chatroom.setRenterId(101L);
        chatroom.setOwnerId(202L);
        chatroom.setPropertyId(303L);
    }

    @Test
    void testCreateChatroom() {
        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(chatroom);

        Chatroom result = chatroomService.createChatroom(101L, 202L, 303L);

        assertEquals(chatroom, result);
        verify(chatroomRepository).save(any(Chatroom.class));
    }

    @Test
    void testCreateChatroomSavesCorrectData() {
        ArgumentCaptor<Chatroom> captor = ArgumentCaptor.forClass(Chatroom.class);
        when(chatroomRepository.save(any(Chatroom.class))).thenReturn(chatroom);

        chatroomService.createChatroom(101L, 202L, 303L);

        verify(chatroomRepository).save(captor.capture());
        Chatroom saved = captor.getValue();

        assertEquals(101L, saved.getRenterId());
        assertEquals(202L, saved.getOwnerId());
        assertEquals(303L, saved.getPropertyId());
    }
}
