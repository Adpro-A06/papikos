package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import lombok.Getter;

import java.util.UUID;

public class EditMessageCommand implements Command {
    private final Chatroom chatroom;
    private final UUID messageId;
    private final String newContent;
    private String oldContent;
    private final MessageRepository messageRepository;
    @Getter
    private Message message;

    public EditMessageCommand(Chatroom chatroom, UUID messageId, String newContent, MessageRepository messageRepository) {
        this.chatroom = chatroom;
        this.messageId = messageId;
        this.newContent = newContent;
        this.messageRepository = messageRepository;
    }

    @Override
    public void execute() {
        // Find message in chatroom
        for (Message msg : chatroom.getMessages()) {
            if (msg.getId().equals(messageId)) {
                this.message = msg;
                this.oldContent = msg.getContent();
                msg.setContent(newContent);
                msg.setEdited(true);

                // Persist changes to repository
                messageRepository.save(msg);
                break;
            }
        }
    }

    @Override
    public void undo() {
        if (message != null) {
            message.setContent(oldContent);
            message.setEdited(false);

            // Persist changes to repository
            messageRepository.save(message);
        }
    }
}