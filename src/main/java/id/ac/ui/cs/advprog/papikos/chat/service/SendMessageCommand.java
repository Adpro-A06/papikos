package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class SendMessageCommand implements Command {
    private final Chatroom chatroom;
    @Getter
    private Message message; // Remove final to allow reassignment
    private final MessageRepository messageRepository;

    public SendMessageCommand(Chatroom chatroom, UUID senderId, String content, MessageRepository messageRepository) {
        this.chatroom = chatroom;
        this.messageRepository = messageRepository;
        this.message = new Message();
        this.message.setSenderId(senderId);
        this.message.setChatroomId(chatroom.getId());
        this.message.setContent(content);
        this.message.setTimestamp(LocalDateTime.now());
    }

    @Override
    public void execute() {
        // Persist message to repository first
        Message savedMessage = messageRepository.save(message);

        // Replace the message reference with the saved one
        this.message = savedMessage;

        // Add the saved message to chatroom's message list
        chatroom.addMessage(savedMessage);
    }

    @Override
    public void undo() {
        // Remove from chatroom using the actual message object
        chatroom.getMessages().remove(message);

        // Remove from repository
        if (message.getId() != null) {
            messageRepository.deleteById(message.getId());
        }
    }
}