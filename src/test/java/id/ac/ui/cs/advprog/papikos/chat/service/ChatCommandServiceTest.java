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
        // Execute SendMessageCommand
        Message newMessage = chatCommandService.sendMessage(chatroom, 202L, "Hello from owner");

        // Verify message was added to chatroom
        assertEquals(2, chatroom.getMessages().size());
        assertEquals(newMessage, chatroom.getMessages().get(1));
        assertEquals("Hello from owner", newMessage.getContent());
        assertEquals(202L, newMessage.getSenderId());
    }

    @Test
    void testEditMessage() {
        // Execute EditMessageCommand
        Message editedMessage = chatCommandService.editMessage(chatroom, message.getId(), "Edited message");

        // Verify message was edited
        assertEquals("Edited message", message.getContent());
        assertEquals(message, editedMessage);
    }

    @Test
    void testDeleteMessage() {
        // Execute DeleteMessageCommand
        boolean deleted = chatCommandService.deleteMessage(chatroom, message.getId());

        // Verify message was deleted
        assertTrue(deleted);
        assertEquals(0, chatroom.getMessages().size());
    }

    @Test
    void testDeleteNonExistentMessage() {
        // Try to delete a message that doesn't exist
        boolean deleted = chatCommandService.deleteMessage(chatroom, 999L);

        // Verify no message was deleted
        assertFalse(deleted);
        assertEquals(1, chatroom.getMessages().size());
    }

    @Test
    void testUndoSendMessage() {
        // Send a message
        Message newMessage = chatCommandService.sendMessage(chatroom, 202L, "Message to undo");
        assertEquals(2, chatroom.getMessages().size());

        // Undo the send operation
        boolean undone = chatCommandService.undoLastCommand();

        // Verify message was removed
        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertFalse(chatroom.getMessages().contains(newMessage));
    }

    @Test
    void testUndoEditMessage() {
        // Remember original content
        String originalContent = message.getContent();

        // Edit a message
        chatCommandService.editMessage(chatroom, message.getId(), "Temporary edit");
        assertEquals("Temporary edit", message.getContent());

        // Undo the edit operation
        boolean undone = chatCommandService.undoLastCommand();

        // Verify message content was restored
        assertTrue(undone);
        assertEquals(originalContent, message.getContent());
    }

    @Test
    void testUndoDeleteMessage() {
        // Delete a message
        chatCommandService.deleteMessage(chatroom, message.getId());
        assertEquals(0, chatroom.getMessages().size());

        // Undo the delete operation
        boolean undone = chatCommandService.undoLastCommand();

        // Verify message was restored
        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().get(0));
    }
}