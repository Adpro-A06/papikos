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

public class CommandTest {

    @Mock
    private MessageRepository messageRepository;

    private Chatroom chatroom;
    private Message message;
    private UUID chatroomId;
    private UUID messageId;
    private UUID senderId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        chatroomId = UUID.randomUUID();
        messageId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        chatroom = new Chatroom();
        chatroom.setId(chatroomId);
        chatroom.setMessages(new ArrayList<>());

        message = new Message();
        message.setId(messageId);
        message.setSenderId(senderId);
        message.setChatroomId(chatroomId);
        message.setContent("Test message");
        message.setTimestamp(LocalDateTime.now());
    }

    @Test
    void sendMessageCommand_Execute_ShouldSaveMessageAndAddToChatroom() {
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        SendMessageCommand command = new SendMessageCommand(chatroom, senderId, "Test content", messageRepository);
        command.execute();

        verify(messageRepository).save(any(Message.class));
        assertEquals(1, chatroom.getMessages().size());
        assertNotNull(command.getMessage());
        assertEquals("Test content", command.getMessage().getContent());
        assertEquals(senderId, command.getMessage().getSenderId());
    }


    @Test
    void sendMessageCommand_Undo_ShouldRemoveMessageFromChatroomAndRepository() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(messageId);
            return msg;
        });

        SendMessageCommand command = new SendMessageCommand(chatroom, senderId, "Test content", messageRepository);
        command.execute();

        assertEquals(1, chatroom.getMessages().size());

        command.undo();

        assertEquals(0, chatroom.getMessages().size());
        verify(messageRepository).deleteById(messageId);
    }

    @Test
    void editMessageCommand_Execute_ShouldUpdateMessageContent() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        String originalContent = message.getContent();
        String newContent = "Updated content";

        EditMessageCommand command = new EditMessageCommand(chatroom, messageId, newContent, messageRepository);
        command.execute();

        verify(messageRepository).findById(messageId);
        verify(messageRepository).save(message);
        assertEquals(newContent, message.getContent());
        assertTrue(message.isEdited());
        assertEquals(message, command.getMessage());
    }

    @Test
    void editMessageCommand_Undo_ShouldRevertMessageContent() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        String originalContent = message.getContent();
        String newContent = "Updated content";

        EditMessageCommand command = new EditMessageCommand(chatroom, messageId, newContent, messageRepository);
        command.execute();
        command.undo();

        assertEquals(originalContent, message.getContent());
        assertFalse(message.isEdited());
        verify(messageRepository, times(2)).save(message);
    }

    @Test
    void editMessageCommand_ExecuteWithNonExistentMessage_ShouldHandleGracefully() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        EditMessageCommand command = new EditMessageCommand(chatroom, messageId, "New content", messageRepository);
        command.execute();

        verify(messageRepository).findById(messageId);
        verify(messageRepository, never()).save(any(Message.class));
        assertNull(command.getMessage());
    }

    @Test
    void deleteMessageCommand_Execute_ShouldDeleteMessageFromRepositoryAndChatroom() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        chatroom.addMessage(message);

        DeleteMessageCommand command = new DeleteMessageCommand(chatroom, messageId, messageRepository);
        command.execute();

        verify(messageRepository).findById(messageId);
        verify(messageRepository).deleteById(messageId);
        assertEquals(0, chatroom.getMessages().size());
        assertTrue(command.isSuccessful());
    }

    @Test
    void deleteMessageCommand_Undo_ShouldRestoreMessage() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);
        chatroom.addMessage(message);

        DeleteMessageCommand command = new DeleteMessageCommand(chatroom, messageId, messageRepository);
        command.execute();
        command.undo();

        verify(messageRepository).save(message);
        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().get(0));
    }

    @Test
    void deleteMessageCommand_ExecuteWithNonExistentMessage_ShouldReturnFalse() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        DeleteMessageCommand command = new DeleteMessageCommand(chatroom, messageId, messageRepository);
        command.execute();

        verify(messageRepository).findById(messageId);
        verify(messageRepository, never()).deleteById(any());
        assertFalse(command.isSuccessful());
    }

    @Test
    void deleteMessageCommand_UndoWithoutExecute_ShouldHandleGracefully() {
        DeleteMessageCommand command = new DeleteMessageCommand(chatroom, messageId, messageRepository);

        command.undo();

        verify(messageRepository, never()).save(any(Message.class));
        assertEquals(0, chatroom.getMessages().size());
    }
}