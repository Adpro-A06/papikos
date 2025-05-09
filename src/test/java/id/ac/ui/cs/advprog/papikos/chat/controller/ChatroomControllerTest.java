package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.service.ChatroomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChatroomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatroomService chatroomService;

    @InjectMocks
    private ChatroomController chatroomController;

    private Chatroom mockChatroom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatroomController).build();

        mockChatroom = new Chatroom();
        mockChatroom.setId(1L);
        mockChatroom.setRenterId(101L);
        mockChatroom.setOwnerId(201L);
        mockChatroom.setPropertyId(301L);
        mockChatroom.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getChatroomsByRenterId_ShouldReturnChatrooms() throws Exception {
        List<Chatroom> mockChatrooms = Arrays.asList(mockChatroom);
        when(chatroomService.getChatroomsByRenterId(anyLong())).thenReturn(mockChatrooms);

        mockMvc.perform(get("/api/chatrooms/renter/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].renterId").value(101))
                .andExpect(jsonPath("$[0].ownerId").value(201))
                .andExpect(jsonPath("$[0].propertyId").value(301));
    }

    @Test
    void getChatroomsByOwnerId_ShouldReturnChatrooms() throws Exception {
        List<Chatroom> mockChatrooms = Arrays.asList(mockChatroom);
        when(chatroomService.getChatroomsByOwnerId(anyLong())).thenReturn(mockChatrooms);

        mockMvc.perform(get("/api/chatrooms/owner/201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].renterId").value(101))
                .andExpect(jsonPath("$[0].ownerId").value(201))
                .andExpect(jsonPath("$[0].propertyId").value(301));
    }

    @Test
    void getChatroomById_ShouldReturnChatroom() throws Exception {
        when(chatroomService.getChatroomById(anyLong())).thenReturn(mockChatroom);

        mockMvc.perform(get("/api/chatrooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.renterId").value(101))
                .andExpect(jsonPath("$.ownerId").value(201))
                .andExpect(jsonPath("$.propertyId").value(301));
    }

    @Test
    void createChatroom_ShouldReturnCreatedChatroom() throws Exception {
        when(chatroomService.createChatroom(anyLong(), anyLong(), anyLong())).thenReturn(mockChatroom);

        mockMvc.perform(post("/api/chatrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"renterId\":101,\"ownerId\":201,\"propertyId\":301}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.renterId").value(101))
                .andExpect(jsonPath("$.ownerId").value(201))
                .andExpect(jsonPath("$.propertyId").value(301));
    }
}