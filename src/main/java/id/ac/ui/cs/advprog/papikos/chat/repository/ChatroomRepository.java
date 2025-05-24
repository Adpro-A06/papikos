package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatroomRepository extends JpaRepository<Chatroom, UUID> {

    @Query("SELECT c FROM Chatroom c WHERE c.renterId = :renterId ORDER BY c.createdAt DESC")
    List<Chatroom> findByRenterId(@Param("renterId") UUID renterId);

    @Query("SELECT c FROM Chatroom c WHERE c.ownerId = :ownerId ORDER BY c.createdAt DESC")
    List<Chatroom> findByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT c FROM Chatroom c WHERE c.renterId = :renterId AND c.ownerId = :ownerId AND c.propertyId = :propertyId")
    Optional<Chatroom> findByRenterIdAndOwnerIdAndPropertyId(
            @Param("renterId") UUID renterId,
            @Param("ownerId") UUID ownerId,
            @Param("propertyId") UUID propertyId
    );

    @Query("SELECT c FROM Chatroom c LEFT JOIN FETCH c.messages m WHERE c.id = :id ORDER BY m.timestamp ASC")
    Optional<Chatroom> findByIdWithMessages(@Param("id") UUID id);

    @Query("SELECT DISTINCT c FROM Chatroom c LEFT JOIN FETCH c.messages m WHERE c.renterId = :renterId ORDER BY c.createdAt DESC")
    List<Chatroom> findByRenterIdWithMessages(@Param("renterId") UUID renterId);

    @Query("SELECT DISTINCT c FROM Chatroom c LEFT JOIN FETCH c.messages m WHERE c.ownerId = :ownerId ORDER BY c.createdAt DESC")
    List<Chatroom> findByOwnerIdWithMessages(@Param("ownerId") UUID ownerId);

    @Query("SELECT c FROM Chatroom c WHERE c.renterId = :renterId ORDER BY c.createdAt DESC")
    List<Chatroom> findByRenterIdForList(@Param("renterId") UUID renterId);

    @Query("SELECT c FROM Chatroom c WHERE c.ownerId = :ownerId ORDER BY c.createdAt DESC")
    List<Chatroom> findByOwnerIdForList(@Param("ownerId") UUID ownerId);
}