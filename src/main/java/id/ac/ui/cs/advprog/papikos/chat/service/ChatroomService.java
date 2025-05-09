package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import java.util.List;

public interface ChatroomService {
    Chatroom createChatroom(Long renterId, Long ownerId, Long propertyId);
    Chatroom getChatroomById(Long id);
    List<Chatroom> getChatroomsByRenterId(Long renterId);
    List<Chatroom> getChatroomsByOwnerId(Long ownerId);
}