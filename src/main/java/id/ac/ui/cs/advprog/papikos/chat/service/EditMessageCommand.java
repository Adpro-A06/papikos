package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import lombok.Getter;

public class EditMessageCommand implements Command {
    private final Chatroom chatroom;
    private final Long messageId;
    private String newContent;
    private String oldContent;
    @Getter
    private Message message;

    public EditMessageCommand(Chatroom chatroom, Long messageId, String newContent) {
        this.chatroom = chatroom;
        this.messageId = messageId;
        this.newContent = newContent;
    }

    @Override
    public void execute() {
        for (Message msg : chatroom.getMessages()) {
            if (msg.getId().equals(messageId)) {
                this.message = msg;
                this.oldContent = msg.getContent();
                msg.setContent(newContent);
                break;
            }
        }
    }

    @Override
    public void undo() {
        if (message != null) {
            message.setContent(oldContent);
        }
    }

}