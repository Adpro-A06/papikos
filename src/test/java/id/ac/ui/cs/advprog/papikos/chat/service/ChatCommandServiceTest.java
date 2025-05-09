package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ChatCommandServiceTest {

    private ChatCommandService chatCommandService;
    private Chatroom chatroom;
    private Message message;

    @BeforeEach
    void setup() {
        chatCommandService = new ChatCommandService();

        chatroom = new Chatroom();
        chatroom.setId(1L);
        chatroom.setRenterId(101L);
        chatroom.setOwnerId(202L);
        chatroom.setPropertyId(303L);
        chatroom.setCreatedAt(LocalDateTime.now());
        chatroom.setMessages(new ArrayList<>());

        message = new Message();
        message.setId(1L);
        message.setSenderId(101L);
        message.setChatroomId(1L);
        message.setContent("Initial message");
        message.setTimestamp(LocalDateTime.now());

        chatroom.addMessage(message);
    }

    @Test
    void testSendMessage() {
        Message newMessage = chatCommandService.sendMessage(chatroom, 202L, "Hello from owner");

        assertEquals(2, chatroom.getMessages().size());
        assertEquals(newMessage, chatroom.getMessages().get(1));
        assertEquals("Hello from owner", newMessage.getContent());
        assertEquals(202L, newMessage.getSenderId());
    }

    @Test
    void testEditMessage() {
        Message editedMessage = chatCommandService.editMessage(chatroom, message.getId(), "Edited message");
        assertEquals("Edited message", message.getContent());
        assertEquals(message, editedMessage);
    }

    @Test
    void testDeleteMessage() {
        boolean deleted = chatCommandService.deleteMessage(chatroom, message.getId());

        assertTrue(deleted);
        assertEquals(0, chatroom.getMessages().size());
    }

    @Test
    void testDeleteNonExistentMessage() {
        boolean deleted = chatCommandService.deleteMessage(chatroom, 999L);

        assertFalse(deleted);
        assertEquals(1, chatroom.getMessages().size());
    }

    @Test
    void testUndoSendMessage() {
        Message newMessage = chatCommandService.sendMessage(chatroom, 202L, "Message to undo");
        assertEquals(2, chatroom.getMessages().size());

        boolean undone = chatCommandService.undoLastCommand(chatroom, newMessage.getId());

        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertFalse(chatroom.getMessages().contains(newMessage));
    }

    @Test
    void testUndoEditMessage() {
        String originalContent = message.getContent();

        chatCommandService.editMessage(chatroom, message.getId(), "Temporary edit");
        assertEquals("Temporary edit", message.getContent());

        boolean undone = chatCommandService.undoLastCommand(chatroom, message.getId());

        assertTrue(undone);
        assertEquals(originalContent, message.getContent());
    }

    @Test
    void testUndoDeleteMessage() {
        chatCommandService.deleteMessage(chatroom, message.getId());
        assertEquals(0, chatroom.getMessages().size());

        boolean undone = chatCommandService.undoLastCommand(chatroom, message.getId());

        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().get(0));
    }
}