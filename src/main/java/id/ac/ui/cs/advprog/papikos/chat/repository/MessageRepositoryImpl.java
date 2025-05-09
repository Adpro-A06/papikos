package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

    private final List<Message> messages = new ArrayList<>();
    private Long idCounter = 1L;

    @Override
    public Message save(Message message) {
        if (message.getId() == null) {
            message.setId(idCounter++);
            messages.add(message);
        } else {
            // Jika message sudah memiliki ID, cek jika perlu diupdate
            Optional<Message> existingMessage = findById(message.getId());
            if (existingMessage.isPresent()) {
                // Hapus pesan lama
                messages.removeIf(m -> m.getId().equals(message.getId()));
                // Tambahkan pesan yang diupdate
                messages.add(message);
            } else {
                // Jika message dengan ID tersebut tidak ada, tambahkan sebagai entry baru
                messages.add(message);
            }
        }
        return message;
    }

    @Override
    public Optional<Message> findById(Long id) {
        return messages.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Message> findByChatroomId(Long chatroomId) {
        return messages.stream()
                .filter(m -> m.getChatroomId().equals(chatroomId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByChatroomIdOrderByTimestampDesc(Long chatroomId) {
        return messages.stream()
                .filter(m -> m.getChatroomId().equals(chatroomId))
                .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        messages.removeIf(m -> m.getId().equals(id));
    }
}