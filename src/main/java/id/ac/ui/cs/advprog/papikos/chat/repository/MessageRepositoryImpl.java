package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MessageRepositoryImpl implements MessageRepository {

    private final List<Message> messages = new ArrayList<>();
    private Long idCounter = 1L;

    @Override
    public Message save(Message message) {
        message.setId(idCounter++);
        messages.add(message);
        return message;
    }

    @Override
    public List<Message> findByChatroomIdOrderByTimestampAsc(Long chatroomId) {
        return messages.stream()
                .filter(m -> m.getChatroomId().equals(chatroomId))
                .sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList());
    }
}
