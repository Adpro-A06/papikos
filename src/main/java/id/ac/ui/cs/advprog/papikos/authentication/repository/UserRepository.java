package id.ac.ui.cs.advprog.papikos.authentication.repository;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    BigDecimal getBalance(UUID userId);
    BigDecimal updateBalance(UUID userId, BigDecimal amount);
}