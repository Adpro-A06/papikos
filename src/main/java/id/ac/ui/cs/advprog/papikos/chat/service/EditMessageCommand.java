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
        message = messageRepository.findById(messageId).orElse(null);

        if (message != null) {
            this.oldContent = message.getContent();
            message.setContent(newContent);
            message.setEdited(true);

            messageRepository.save(message);
        }
    }

    @Override
    public void undo() {
        if (message != null) {
            message.setContent(oldContent);
            message.setEdited(false);

            messageRepository.save(message);
        }
    }
}