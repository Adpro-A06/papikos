package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.service.MessageServiceImpl;
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

public class MessageRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MessageServiceImpl messageService;

    @InjectMocks
    private MessageRestController messageController;

    private Message mockMessage;
    private UUID chatroomId;
    private UUID messageId;
    private UUID senderId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();

        chatroomId = UUID.randomUUID();
        messageId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        mockMessage = new Message();
        mockMessage.setId(messageId);
        mockMessage.setChatroomId(chatroomId);
        mockMessage.setSenderId(senderId);
        mockMessage.setContent("Test message");
        mockMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void getMessagesByChatroomId_ShouldReturnMessages() throws Exception {
        List<Message> mockMessages = Arrays.asList(mockMessage);
        when(messageService.getMessagesByChatroomId(any(UUID.class))).thenReturn(mockMessages);

        mockMvc.perform(get("/api/chatrooms/{chatroomId}/messages", chatroomId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(messageId.toString()))
                .andExpect(jsonPath("$[0].chatroomId").value(chatroomId.toString()))
                .andExpect(jsonPath("$[0].senderId").value(senderId.toString()))
                .andExpect(jsonPath("$[0].content").value("Test message"));
    }

    @Test
    void sendMessage_ShouldReturnSentMessage() throws Exception {
        when(messageService.sendMessage(any(UUID.class), any(UUID.class), anyString())).thenReturn(mockMessage);

        mockMvc.perform(post("/api/chatrooms/{chatroomId}/messages", chatroomId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senderId\":\"" + senderId.toString() + "\",\"content\":\"Test message\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.chatroomId").value(chatroomId.toString()))
                .andExpect(jsonPath("$.senderId").value(senderId.toString()))
                .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    void editMessage_ShouldReturnEditedMessage() throws Exception {
        mockMessage.setContent("Edited message");
        mockMessage.setEdited(true);
        when(messageService.editMessage(any(UUID.class), any(UUID.class), anyString())).thenReturn(mockMessage);

        mockMvc.perform(put("/api/chatrooms/{chatroomId}/messages/{messageId}", chatroomId.toString(), messageId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Edited message\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.chatroomId").value(chatroomId.toString()))
                .andExpect(jsonPath("$.senderId").value(senderId.toString()))
                .andExpect(jsonPath("$.content").value("Edited message"));
    }

    @Test
    void deleteMessage_ShouldReturnSuccess() throws Exception {
        when(messageService.deleteMessage(any(UUID.class), any(UUID.class))).thenReturn(true);

        mockMvc.perform(delete("/api/chatrooms/{chatroomId}/messages/{messageId}", chatroomId.toString(), messageId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteMessage_ShouldReturnFailure() throws Exception {
        when(messageService.deleteMessage(any(UUID.class), any(UUID.class))).thenReturn(false);

        mockMvc.perform(delete("/api/chatrooms/{chatroomId}/messages/{messageId}", chatroomId.toString(), messageId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void undoLastAction_ShouldReturnSuccess() throws Exception {
        when(messageService.undoLastAction(any(UUID.class), any(UUID.class))).thenReturn(true);

        mockMvc.perform(post("/api/chatrooms/{chatroomId}/messages/{messageId}/undo", chatroomId.toString(), messageId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void undoLastAction_ShouldReturnFailure() throws Exception {
        when(messageService.undoLastAction(any(UUID.class), any(UUID.class))).thenReturn(false);

        mockMvc.perform(post("/api/chatrooms/{chatroomId}/messages/{messageId}/undo", chatroomId.toString(), messageId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}