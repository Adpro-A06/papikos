package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.service.ChatroomServiceImpl;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChatroomRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatroomServiceImpl chatroomService;

    @InjectMocks
    private ChatroomRestController chatroomRestController;

    private Chatroom mockChatroom;
    private UUID propertyId;
    private UUID chatroomId;
    private UUID renterId;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatroomRestController).build();

        propertyId = UUID.randomUUID();
        chatroomId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        mockChatroom = new Chatroom();
        mockChatroom.setId(chatroomId);
        mockChatroom.setRenterId(renterId);
        mockChatroom.setOwnerId(ownerId);
        mockChatroom.setPropertyId(propertyId);
        mockChatroom.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getChatroomsByRenterId_ShouldReturnChatrooms() throws Exception {
        List<Chatroom> mockChatrooms = Arrays.asList(mockChatroom);
        when(chatroomService.getChatroomsByRenterId(any(UUID.class))).thenReturn(mockChatrooms);

        mockMvc.perform(get("/api/chatrooms/renter/{renterId}", renterId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(chatroomId.toString()))
                .andExpect(jsonPath("$[0].renterId").value(renterId.toString()))
                .andExpect(jsonPath("$[0].ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$[0].propertyId").value(propertyId.toString()));
    }

    @Test
    void getChatroomsByOwnerId_ShouldReturnChatrooms() throws Exception {
        List<Chatroom> mockChatrooms = Arrays.asList(mockChatroom);
        when(chatroomService.getChatroomsByOwnerId(any(UUID.class))).thenReturn(mockChatrooms);

        mockMvc.perform(get("/api/chatrooms/owner/{ownerId}", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(chatroomId.toString()))
                .andExpect(jsonPath("$[0].renterId").value(renterId.toString()))
                .andExpect(jsonPath("$[0].ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$[0].propertyId").value(propertyId.toString()));
    }

    @Test
    void getChatroomById_ShouldReturnChatroom() throws Exception {
        when(chatroomService.getChatroomById(any(UUID.class))).thenReturn(mockChatroom);

        mockMvc.perform(get("/api/chatrooms/{id}", chatroomId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chatroomId.toString()))
                .andExpect(jsonPath("$.renterId").value(renterId.toString()))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.propertyId").value(propertyId.toString()));
    }

    @Test
    void getChatroomById_ShouldReturnNotFound_WhenChatroomNotExists() throws Exception {
        when(chatroomService.getChatroomById(any(UUID.class)))
                .thenThrow(new RuntimeException("Chatroom not found"));

        mockMvc.perform(get("/api/chatrooms/{id}", chatroomId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createChatroom_ShouldReturnCreatedChatroom() throws Exception {
        when(chatroomService.createChatroom(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(mockChatroom);

        mockMvc.perform(post("/api/chatrooms/create/{kosId}", propertyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"renterId\":\"" + renterId.toString() + "\",\"ownerId\":\"" + ownerId.toString() + "\",\"propertyId\":\"" + propertyId.toString() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chatroomId").value(chatroomId.toString()))
                .andExpect(jsonPath("$.renterId").value(renterId.toString()))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.propertyId").value(propertyId.toString()))
                .andExpect(jsonPath("$.message").value("Chatroom created successfully"));
    }

    @Test
    void createChatroom_ShouldReturnInternalServerError_WhenExceptionOccurs() throws Exception {
        when(chatroomService.createChatroom(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/chatrooms/create/{kosId}", propertyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"renterId\":\"" + renterId.toString() + "\",\"ownerId\":\"" + ownerId.toString() + "\",\"propertyId\":\"" + propertyId.toString() + "\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create chatroom: Database error"));
    }
}