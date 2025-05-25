package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.Stack;
import java.util.UUID;

@Service
public class ChatCommandService {
    private final Stack<Command> commandHistory;
    private final MessageRepository messageRepository;

    public ChatCommandService(MessageRepository messageRepository) {
        this.commandHistory = new Stack<>();
        this.messageRepository = messageRepository;
    }

    public Message sendMessage(Chatroom chatroom, UUID senderId, String content) {
        SendMessageCommand command = new SendMessageCommand(chatroom, senderId, content, messageRepository);
        command.execute();
        commandHistory.push(command);
        return command.getMessage();
    }

    public Message editMessage(Chatroom chatroom, UUID messageId, String newContent) {
        EditMessageCommand command = new EditMessageCommand(chatroom, messageId, newContent, messageRepository);
        command.execute();
        commandHistory.push(command);
        return command.getMessage();
    }

    public boolean deleteMessage(Chatroom chatroom, UUID messageId) {
        DeleteMessageCommand command = new DeleteMessageCommand(chatroom, messageId, messageRepository);
        command.execute();
        commandHistory.push(command);
        return command.isSuccessful();
    }

    public boolean undoLastCommand(Chatroom chatroom, UUID messageId) {
        if (!commandHistory.isEmpty()) {
            Command command = commandHistory.pop();
            command.undo();
            return true;
        }
        return false;
    }
}