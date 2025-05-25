package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;

import java.util.UUID;

public class DeleteMessageCommand implements Command {
    private final Chatroom chatroom;
    private final UUID messageId;
    private final MessageRepository messageRepository;
    private Message deletedMessage;

    public DeleteMessageCommand(Chatroom chatroom, UUID messageId, MessageRepository messageRepository) {
        this.chatroom = chatroom;
        this.messageId = messageId;
        this.messageRepository = messageRepository;
    }

    @Override
    public void execute() {
        deletedMessage = messageRepository.findById(messageId).orElse(null);

        if (deletedMessage != null) {
            messageRepository.deleteById(messageId);
            chatroom.getMessages().remove(deletedMessage);
        }
    }

    @Override
    public void undo() {
        if (deletedMessage != null) {
            Message restoredMessage = messageRepository.save(deletedMessage);

            if (!chatroom.getMessages().contains(restoredMessage)) {
                chatroom.addMessage(restoredMessage);
            }
        }
    }

    public boolean isSuccessful() {
        return deletedMessage != null;
    }
}