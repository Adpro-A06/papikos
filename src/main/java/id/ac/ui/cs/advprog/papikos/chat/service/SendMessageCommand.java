package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepositoryImpl;
import lombok.Getter;

import java.time.LocalDateTime;

public class SendMessageCommand implements Command {
//    private final MessageRepositoryImpl messageRepository
    private final Chatroom chatroom;
    @Getter
    private final Message message;

    public SendMessageCommand(Chatroom chatroom, Long senderId, String content) {
        this.chatroom = chatroom;
        this.message = new Message();
        this.message.setSenderId(senderId);
        this.message.setChatroomId(chatroom.getId());
        this.message.setContent(content);
        this.message.setTimestamp(LocalDateTime.now());
    }

    @Override
    public void execute() {
//        message.setId(generateId());
        chatroom.addMessage(message);
    }

    @Override
    public void undo() {
        chatroom.getMessages().remove(message);
    }

}