package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChatCommandServiceTest {

    private ChatCommandService chatCommandService;
    private Chatroom chatroom;
    private Message message;
    private UUID propertyId;
    private UUID chatroomId;
    private UUID renterId;
    private UUID ownerId;
    private UUID messageId;
    private MessageRepository messageRepository; // Mock the MessageRepository

    @BeforeEach
    void setup() {
        // Mocking the MessageRepository
        messageRepository = mock(MessageRepository.class);

        // Create the service instance with mocked MessageRepository
        chatCommandService = new ChatCommandService(messageRepository);

        propertyId = UUID.randomUUID();
        chatroomId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        messageId = UUID.randomUUID();

        chatroom = new Chatroom();
        chatroom.setId(chatroomId);
        chatroom.setRenterId(renterId);
        chatroom.setOwnerId(ownerId);
        chatroom.setPropertyId(propertyId);
        chatroom.setCreatedAt(LocalDateTime.now());
        chatroom.setMessages(new ArrayList<>());

        message = new Message();
        message.setId(messageId);
        message.setSenderId(renterId);
        message.setChatroomId(chatroomId);
        message.setContent("Initial message");
        message.setTimestamp(LocalDateTime.now());

        chatroom.addMessage(message);

        // Mock repository behavior - return the same object that was passed in, but with ID set
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message messageToSave = invocation.getArgument(0);
            // If the message doesn't have an ID, set one
            if (messageToSave.getId() == null) {
                messageToSave.setId(UUID.randomUUID());
            }
            return messageToSave;
        });
    }

    @Test
    void testSendMessage() {
        // Test sending a new message
        Message newMessage = chatCommandService.sendMessage(chatroom, ownerId, "Hello from owner");

        // Check that the message was added to the chatroom
        assertEquals(2, chatroom.getMessages().size());
        assertEquals(newMessage, chatroom.getMessages().get(1));
        assertEquals("Hello from owner", newMessage.getContent());
        assertEquals(ownerId, newMessage.getSenderId());
    }

    @Test
    void testEditMessage() {
        // Test editing the message
        Message editedMessage = chatCommandService.editMessage(chatroom, message.getId(), "Edited message");

        // Verify that the message content was updated
        assertEquals("Edited message", message.getContent());
        assertEquals(message, editedMessage);
    }

    @Test
    void testDeleteMessage() {
        // Test deleting the message
        boolean deleted = chatCommandService.deleteMessage(chatroom, message.getId());

        // Check that the message was deleted
        assertTrue(deleted);
        assertEquals(0, chatroom.getMessages().size());
    }

    @Test
    void testDeleteNonExistentMessage() {
        // Test trying to delete a non-existent message
        boolean deleted = chatCommandService.deleteMessage(chatroom, UUID.randomUUID());

        // Ensure it returns false
        assertFalse(deleted);
        assertEquals(1, chatroom.getMessages().size());
    }

    @Test
    void testUndoSendMessage() {
        // Test undoing the sending of a message
        Message newMessage = chatCommandService.sendMessage(chatroom, ownerId, "Message to undo");
        assertEquals(2, chatroom.getMessages().size());

        boolean undone = chatCommandService.undoLastCommand(chatroom, newMessage.getId());

        // Ensure the message was undone
        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertFalse(chatroom.getMessages().contains(newMessage));
    }

    @Test
    void testUndoEditMessage() {
        // Save the original message content
        String originalContent = message.getContent();

        // Edit the message content
        chatCommandService.editMessage(chatroom, message.getId(), "Temporary edit");
        assertEquals("Temporary edit", message.getContent());

        // Undo the edit
        boolean undone = chatCommandService.undoLastCommand(chatroom, message.getId());

        // Ensure the content is reverted to the original
        assertTrue(undone);
        assertEquals(originalContent, message.getContent());
    }

    @Test
    void testUndoDeleteMessage() {
        // Delete the message
        chatCommandService.deleteMessage(chatroom, message.getId());
        assertEquals(0, chatroom.getMessages().size());

        // Undo the delete operation
        boolean undone = chatCommandService.undoLastCommand(chatroom, message.getId());

        // Ensure the message was restored
        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().get(0));
    }
}