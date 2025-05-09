package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.service.MessageService;
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

public class MessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    private Message mockMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();

        mockMessage = new Message();
        mockMessage.setId(1L);
        mockMessage.setChatroomId(1L);
        mockMessage.setSenderId(101L);
        mockMessage.setContent("Test message");
        mockMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void getMessagesByChatroomId_ShouldReturnMessages() throws Exception {
        List<Message> mockMessages = Arrays.asList(mockMessage);
        when(messageService.getMessagesByChatroomId(anyLong())).thenReturn(mockMessages);

        mockMvc.perform(get("/api/chatrooms/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].chatroomId").value(1))
                .andExpect(jsonPath("$[0].senderId").value(101))
                .andExpect(jsonPath("$[0].content").value("Test message"));
    }

    @Test
    void sendMessage_ShouldReturnSentMessage() throws Exception {
        when(messageService.sendMessage(anyLong(), anyLong(), anyString())).thenReturn(mockMessage);

        mockMvc.perform(post("/api/chatrooms/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senderId\":101,\"content\":\"Test message\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.chatroomId").value(1))
                .andExpect(jsonPath("$.senderId").value(101))
                .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    void editMessage_ShouldReturnEditedMessage() throws Exception {
        when(messageService.editMessage(anyLong(), anyLong(), anyString())).thenReturn(mockMessage);

        mockMvc.perform(put("/api/chatrooms/1/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Edited message\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.chatroomId").value(1))
                .andExpect(jsonPath("$.senderId").value(101));
    }

    @Test
    void deleteMessage_ShouldReturnSuccess() throws Exception {
        when(messageService.deleteMessage(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/chatrooms/1/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void undoLastAction_ShouldReturnSuccess() throws Exception {
        when(messageService.undoLastAction(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/api/chatrooms/1/messages/1/undo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}