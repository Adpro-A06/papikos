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
    private int originalIndex;

    public DeleteMessageCommand(Chatroom chatroom, UUID messageId, MessageRepository messageRepository) {
        this.chatroom = chatroom;
        this.messageId = messageId;
        this.messageRepository = messageRepository;
    }

    @Override
    public void execute() {
        for (int i = 0; i < chatroom.getMessages().size(); i++) {
            Message message = chatroom.getMessages().get(i);
            if (message.getId().equals(messageId)) {
                deletedMessage = message;
                originalIndex = i;

                // Remove from chatroom
                chatroom.getMessages().remove(i);

                // Remove from repository
                messageRepository.deleteById(messageId);
                break;
            }
        }
    }

    @Override
    public void undo() {
        if (deletedMessage != null) {
            // Restore to repository
            messageRepository.save(deletedMessage);

            // Restore to chatroom
            if (originalIndex <= chatroom.getMessages().size()) {
                chatroom.getMessages().add(originalIndex, deletedMessage);
            } else {
                chatroom.getMessages().add(deletedMessage);
            }
        }
    }

    public boolean isSuccessful() {
        return deletedMessage != null;
    }
}