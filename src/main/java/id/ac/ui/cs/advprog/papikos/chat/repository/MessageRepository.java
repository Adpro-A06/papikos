package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.chatroom.id = :chatroomId ORDER BY m.timestamp ASC")
    List<Message> findByChatroomId(@Param("chatroomId") UUID chatroomId);

    @Query("SELECT m FROM Message m WHERE m.chatroom.id = :chatroomId ORDER BY m.timestamp DESC")
    List<Message> findByChatroomIdOrderByTimestampDesc(@Param("chatroomId") UUID chatroomId);

    @Query("SELECT m FROM Message m WHERE m.chatroom.id = :chatroomId AND m.isDeleted = false ORDER BY m.timestamp ASC")
    List<Message> findActiveByChatroomId(@Param("chatroomId") UUID chatroomId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatroom.id = :chatroomId AND m.senderId != :userId AND m.isRead = false")
    int countUnreadMessagesByChatroomIdAndUserId(@Param("chatroomId") UUID chatroomId, @Param("userId") UUID userId);
}