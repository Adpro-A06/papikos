package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ChatroomRepositoryImpl implements ChatroomRepository {

    private final List<Chatroom> chatrooms = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public Chatroom save(Chatroom chatroom) {
        if (chatroom.getId() == null) {
            // New chatroom
            chatroom.setId(idCounter.getAndIncrement());
            chatrooms.add(chatroom);
        } else {
            // Update existing chatroom
            for (int i = 0; i < chatrooms.size(); i++) {
                if (chatrooms.get(i).getId().equals(chatroom.getId())) {
                    chatrooms.set(i, chatroom);
                    break;
                }
            }
        }
        return chatroom;
    }

    @Override
    public Optional<Chatroom> findById(Long id) {
        return chatrooms.stream().filter(chatroom -> chatroom.getId().equals(id)).findFirst();
    }

    @Override
    public List<Chatroom> findByRenterId(Long renterId) {
        return chatrooms.stream()
                .filter(chatroom -> chatroom.getRenterId().equals(renterId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Chatroom> findByOwnerId(Long ownerId) {
        return chatrooms.stream()
                .filter(chatroom -> chatroom.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Chatroom> findByRenterIdAndOwnerIdAndPropertyId(Long renterId, Long ownerId, Long propertyId) {
        return chatrooms.stream()
                .filter(c -> c.getRenterId().equals(renterId)
                        && c.getOwnerId().equals(ownerId)
                        && c.getPropertyId().equals(propertyId))
                .findFirst();
    }
}