package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ChatCommandServiceTest {

    @Mock
    private MessageRepository messageRepository;

    private ChatCommandService chatCommandService;
    private Chatroom chatroom;
    private Message message;
    private UUID propertyId;
    private UUID chatroomId;
    private UUID renterId;
    private UUID ownerId;
    private UUID messageId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

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
    }

    @Test
    void testSendMessage() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message messageToSave = invocation.getArgument(0);
            if (messageToSave.getId() == null) {
                messageToSave.setId(UUID.randomUUID());
            }
            return messageToSave;
        });

        Message result = chatCommandService.sendMessage(chatroom, ownerId, "Hello from owner");

        assertNotNull(result);
        assertEquals("Hello from owner", result.getContent());
        assertEquals(ownerId, result.getSenderId());
        assertEquals(2, chatroom.getMessages().size());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testEditMessage() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        String originalContent = message.getContent();
        Message editedMessage = chatCommandService.editMessage(chatroom, messageId, "Edited message");

        assertEquals("Edited message", message.getContent());
        assertTrue(message.isEdited());
        assertEquals(message, editedMessage);

        verify(messageRepository).findById(messageId);
        verify(messageRepository).save(message);
    }

    @Test
    void testEditMessage_MessageNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(messageRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Message result = chatCommandService.editMessage(chatroom, nonExistentId, "Edited message");

        assertNull(result);
        verify(messageRepository).findById(nonExistentId);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testDeleteMessage() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        boolean deleted = chatCommandService.deleteMessage(chatroom, messageId);

        assertTrue(deleted);
        assertEquals(0, chatroom.getMessages().size());

        verify(messageRepository).findById(messageId);
        verify(messageRepository).deleteById(messageId);
    }

    @Test
    void testDeleteMessage_MessageNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(messageRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        boolean deleted = chatCommandService.deleteMessage(chatroom, nonExistentId);

        assertFalse(deleted);
        assertEquals(1, chatroom.getMessages().size());

        verify(messageRepository).findById(nonExistentId);
        verify(messageRepository, never()).deleteById(any());
    }

    @Test
    void testUndoSendMessage() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message messageToSave = invocation.getArgument(0);
            if (messageToSave.getId() == null) {
                messageToSave.setId(UUID.randomUUID());
            }
            return messageToSave;
        });

        // Send a message
        Message newMessage = chatCommandService.sendMessage(chatroom, ownerId, "Message to undo");
        assertEquals(2, chatroom.getMessages().size());

        // Undo the send
        boolean undone = chatCommandService.undoLastCommand(chatroom, newMessage.getId());

        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertFalse(chatroom.getMessages().contains(newMessage));

        verify(messageRepository).deleteById(newMessage.getId());
    }

    @Test
    void testUndoEditMessage() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        String originalContent = message.getContent();

        // Edit the message
        chatCommandService.editMessage(chatroom, messageId, "Temporary edit");
        assertEquals("Temporary edit", message.getContent());
        assertTrue(message.isEdited());

        // Undo the edit
        boolean undone = chatCommandService.undoLastCommand(chatroom, messageId);

        assertTrue(undone);
        assertEquals(originalContent, message.getContent());
        assertFalse(message.isEdited());

        verify(messageRepository, times(2)).save(message); // Once for edit, once for undo
    }

    @Test
    void testUndoDeleteMessage() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        // Delete the message
        chatCommandService.deleteMessage(chatroom, messageId);
        assertEquals(0, chatroom.getMessages().size());

        // Undo the delete
        boolean undone = chatCommandService.undoLastCommand(chatroom, messageId);

        assertTrue(undone);
        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().get(0));

        verify(messageRepository).save(message); // To restore the message
    }

    @Test
    void testUndoWithEmptyCommandHistory() {
        // Create a new service instance to ensure empty command history
        ChatCommandService freshService = new ChatCommandService(messageRepository);

        boolean undone = freshService.undoLastCommand(chatroom, messageId);

        assertFalse(undone);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void testMultipleCommandsAndUndo() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message messageToSave = invocation.getArgument(0);
            if (messageToSave.getId() == null) {
                messageToSave.setId(UUID.randomUUID());
            }
            return messageToSave;
        });
        when(messageRepository.findById(any(UUID.class))).thenReturn(Optional.of(message));

        // Send a message
        Message newMessage = chatCommandService.sendMessage(chatroom, ownerId, "New message");
        assertEquals(2, chatroom.getMessages().size());

        // Edit the original message
        chatCommandService.editMessage(chatroom, messageId, "Edited content");
        assertEquals("Edited content", message.getContent());

        // Undo last command (edit)
        boolean firstUndo = chatCommandService.undoLastCommand(chatroom, messageId);
        assertTrue(firstUndo);
        assertEquals("Initial message", message.getContent());

        // Undo previous command (send)
        boolean secondUndo = chatCommandService.undoLastCommand(chatroom, newMessage.getId());
        assertTrue(secondUndo);
        assertEquals(1, chatroom.getMessages().size());
        assertFalse(chatroom.getMessages().contains(newMessage));
    }

    @Test
    void testCommandHistoryMaintainsOrder() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message messageToSave = invocation.getArgument(0);
            if (messageToSave.getId() == null) {
                messageToSave.setId(UUID.randomUUID());
            }
            return messageToSave;
        });
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        // Perform multiple operations
        Message msg1 = chatCommandService.sendMessage(chatroom, ownerId, "Message 1");
        Message msg2 = chatCommandService.sendMessage(chatroom, ownerId, "Message 2");
        chatCommandService.editMessage(chatroom, messageId, "Edited original");

        assertEquals(3, chatroom.getMessages().size());
        assertEquals("Edited original", message.getContent());

        // Undo should reverse in LIFO order
        // First undo: edit command
        chatCommandService.undoLastCommand(chatroom, messageId);
        assertEquals("Initial message", message.getContent());

        // Second undo: send message 2
        chatCommandService.undoLastCommand(chatroom, msg2.getId());
        assertEquals(2, chatroom.getMessages().size());
        assertFalse(chatroom.getMessages().contains(msg2));

        // Third undo: send message 1
        chatCommandService.undoLastCommand(chatroom, msg1.getId());
        assertEquals(1, chatroom.getMessages().size());
        assertFalse(chatroom.getMessages().contains(msg1));
    }

    @Test
    void testSendMessageWithNullContent() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message messageToSave = invocation.getArgument(0);
            if (messageToSave.getId() == null) {
                messageToSave.setId(UUID.randomUUID());
            }
            return messageToSave;
        });

        Message result = chatCommandService.sendMessage(chatroom, ownerId, null);

        assertNotNull(result);
        assertNull(result.getContent());
        assertEquals(ownerId, result.getSenderId());
        assertEquals(2, chatroom.getMessages().size());
    }

    @Test
    void testSendMessageWithEmptyContent() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message messageToSave = invocation.getArgument(0);
            if (messageToSave.getId() == null) {
                messageToSave.setId(UUID.randomUUID());
            }
            return messageToSave;
        });

        Message result = chatCommandService.sendMessage(chatroom, ownerId, "");

        assertNotNull(result);
        assertEquals("", result.getContent());
        assertEquals(ownerId, result.getSenderId());
        assertEquals(2, chatroom.getMessages().size());
    }

    @Test
    void testEditMessageWithNullContent() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        Message result = chatCommandService.editMessage(chatroom, messageId, null);

        assertNotNull(result);
        assertNull(message.getContent());
        assertTrue(message.isEdited());

        verify(messageRepository).save(message);
    }
}