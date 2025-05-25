package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class SendMessageCommand implements Command {
    private Chatroom chatroom;
    private UUID senderId;
    private String content;
    @Getter
    private Message message;
    private MessageRepository messageRepository;

    public SendMessageCommand(Chatroom chatroom, UUID senderId, String content, MessageRepository messageRepository) {
        this.chatroom = chatroom;
        this.senderId = senderId;
        this.content = content;
        this.messageRepository = messageRepository;
    }

    @Override
    public void execute() {
        message = new Message();
        message.setSenderId(senderId);
        message.setContent(content);
        message.setChatroomId(chatroom.getId());
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message);

        if (!chatroom.getMessages().contains(message)) {
            chatroom.addMessage(message);
        }
    }

    @Override
    public void undo() {
        chatroom.getMessages().remove(message);

        if (message.getId() != null) {
            messageRepository.deleteById(message.getId());
        }
    }
}