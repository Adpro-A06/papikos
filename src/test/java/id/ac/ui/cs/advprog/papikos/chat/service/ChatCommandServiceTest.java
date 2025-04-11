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
        // Mengeksekusi SendMessageCommand
        Message newMessage = chatCommandService.sendMessage(chatroom, 202L, "Hello from owner");

        // Verifikasi message sudah ditambahkan ke chatroom
        assertEquals(2, chatroom.getMessages().size());
        assertEquals(newMessage, chatroom.getMessages().get(1));
        assertEquals("Hello from owner", newMessage.getContent());
        assertEquals(202L, newMessage.getSenderId());
    }

    @Test
    void testEditMessage() {
        // Mengeksekusi EditMessageCommand
        Message editedMessage = chatCommandService.editMessage(chatroom, message.getId(), "Edited message");

        // Verifikasi message sudah diedit
        assertEquals("Edited message", message.getContent());
        assertEquals(message, editedMessage);
    }

    @Test
    void testDeleteMessage() {
        // Mengeksekusi DeleteMessageCommand
        boolean deleted = chatCommandService.deleteMessage(chatroom, message.getId());

        // Verifikasi message sudah dihapus
        assertTrue(deleted);
        assertEquals(0, chatroom.getMessages().size());
    }

    @Test
    void testDeleteNonExistentMessage() {
        // Mencoba mendelete message yang tidak ada
        boolean deleted = chatCommandService.deleteMessage(chatroom, 999L);

        // Verifikasi tidak ada message yang tidak dihapus
        assertFalse(deleted);
        assertEquals(1, chatroom.getMessages().size());
    }

    @Test
    void testUndoSendMessage() {
        // Mengirim pesan
        Message newMessage = chatCommandService.sendMessage(chatroom, 202L, "Message to undo");
        assertEquals(2, chatroom.getMessages().size());

        // Undo operasi send
        boolean undone = chatCommandService.undoLastCommand();

        // Verifikasi message sudah diremove
        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertFalse(chatroom.getMessages().contains(newMessage));
    }

    @Test
    void testUndoEditMessage() {
        // Mengingat original content
        String originalContent = message.getContent();

        // Edit message
        chatCommandService.editMessage(chatroom, message.getId(), "Temporary edit");
        assertEquals("Temporary edit", message.getContent());

        // Undo operasi edit
        boolean undone = chatCommandService.undoLastCommand();

        // Verifikasi content message direstore
        assertTrue(undone);
        assertEquals(originalContent, message.getContent());
    }

    @Test
    void testUndoDeleteMessage() {
        // Menghapus pesan
        chatCommandService.deleteMessage(chatroom, message.getId());
        assertEquals(0, chatroom.getMessages().size());

        // Undo operasi delete
        boolean undone = chatCommandService.undoLastCommand();

        // Verifikasi pesan sudah direstore
        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().get(0));
    }
}