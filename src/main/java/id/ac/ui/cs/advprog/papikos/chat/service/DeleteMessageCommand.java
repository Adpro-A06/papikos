package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;

public class DeleteMessageCommand implements Command {
    private final Chatroom chatroom;
    private final Long messageId;
    private Message deletedMessage;
    private int originalIndex;

    public DeleteMessageCommand(Chatroom chatroom, Long messageId) {
        this.chatroom = chatroom;
        this.messageId = messageId;
    }

    @Override
    public void execute() {
        for (int i = 0; i < chatroom.getMessages().size(); i++) {
            Message message = chatroom.getMessages().get(i);
            if (message.getId().equals(messageId)) {
                deletedMessage = message;
                originalIndex = i;
                chatroom.getMessages().remove(i);
                break;
            }
        }
    }

    @Override
    public void undo() {
        if (deletedMessage != null && originalIndex <= chatroom.getMessages().size()) {
            chatroom.getMessages().add(originalIndex, deletedMessage);
        } else if (deletedMessage != null) {
            chatroom.getMessages().add(deletedMessage);
        }
    }

    public boolean isSuccessful() {
        return deletedMessage != null;
    }
}