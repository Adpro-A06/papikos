package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class ChatroomRepositoryImpl {

    private final List<Chatroom> chatrooms = new CopyOnWriteArrayList<>();

//    @Override
    public Chatroom save(Chatroom chatroom) {
        if (chatroom.getId() == null) {
            // New chatroom, generate UUID
            chatroom.setId(UUID.randomUUID());
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

    // @Override
    public Optional<Chatroom> findById(UUID id) {
        return chatrooms.stream().filter(chatroom -> chatroom.getId().equals(id)).findFirst();
    }

    // @Override
    public List<Chatroom> findByRenterId(UUID renterId) {
        return chatrooms.stream()
                .filter(chatroom -> chatroom.getRenterId().equals(renterId))
                .collect(Collectors.toList());
    }

    // @Override
    public List<Chatroom> findByOwnerId(UUID ownerId) {
        return chatrooms.stream()
                .filter(chatroom -> chatroom.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    // @Override
    public Optional<Chatroom> findByRenterIdAndOwnerIdAndPropertyId(UUID renterId, UUID ownerId, UUID propertyId) {
        return chatrooms.stream()
                .filter(c -> c.getRenterId().equals(renterId)
                        && c.getOwnerId().equals(ownerId)
                        && c.getPropertyId().equals(propertyId))
                .findFirst();
    }
}
