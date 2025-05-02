package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import java.util.List;

public interface ChatroomRepository {
    Chatroom save(Chatroom chatroom);
    List<Chatroom> findByRenterId(Long renterId);
    List<Chatroom> findByOwnerId(Long ownerId);
}