package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;

import java.util.Stack;

public class ChatCommandService {
    private final Stack<Command> commandHistory;

    public ChatCommandService() {
        this.commandHistory = new Stack<>();
    }

    public Message sendMessage(Chatroom chatroom, Long senderId, String content) {
        SendMessageCommand command = new SendMessageCommand(chatroom, senderId, content);
        command.execute();
        commandHistory.push(command);
        return command.getMessage();
    }

    public Message editMessage(Chatroom chatroom, Long messageId, String newContent) {
        EditMessageCommand command = new EditMessageCommand(chatroom, messageId, newContent);
        command.execute();
        commandHistory.push(command);
        return command.getMessage();
    }

    public boolean deleteMessage(Chatroom chatroom, Long messageId) {
        DeleteMessageCommand command = new DeleteMessageCommand(chatroom, messageId);
        command.execute();
        commandHistory.push(command);
        return command.isSuccessful();
    }

    public boolean undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            Command command = commandHistory.pop();
            command.undo();
            return true;
        }
        return false;
    }
}