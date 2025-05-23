package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

    private final List<Message> messages = new ArrayList<>();
    private Long idCounter = 1L; // The counter for UUIDs might be obsolete, but can still be used for any temporary ID generation

    @Override
    public Message save(Message message) {
        if (message.getId() == null) {
            // If message doesn't have an ID, generate it as UUID
            message.setId(UUID.randomUUID());
            messages.add(message);
        } else {
            // If message already has an ID, check if it needs to be updated
            Optional<Message> existingMessage = findById(message.getId());
            if (existingMessage.isPresent()) {
                // Remove old message
                messages.removeIf(m -> m.getId().equals(message.getId()));
                // Add updated message
                messages.add(message);
            } else {
                // If message with that ID does not exist, add it as a new entry
                messages.add(message);
            }
        }
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return messages.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Message> findByChatroomId(UUID chatroomId) {
        return messages.stream()
                .filter(m -> m.getChatroomId().equals(chatroomId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByChatroomIdOrderByTimestampDesc(UUID chatroomId) {
        return messages.stream()
                .filter(m -> m.getChatroomId().equals(chatroomId))
                .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        messages.removeIf(m -> m.getId().equals(id));
    }
}
