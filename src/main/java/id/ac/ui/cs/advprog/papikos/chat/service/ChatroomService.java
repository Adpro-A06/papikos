package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import java.util.List;
import java.util.UUID;

public interface ChatroomService {
    Chatroom createChatroom(UUID renterId, UUID ownerId, UUID propertyId);
    Chatroom getChatroomById(UUID id);
    List<Chatroom> getChatroomsByRenterId(UUID renterId);
    List<Chatroom> getChatroomsByOwnerId(UUID ownerId);
}