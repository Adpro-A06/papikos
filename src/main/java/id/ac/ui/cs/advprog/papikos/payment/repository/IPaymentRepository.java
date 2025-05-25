package id.ac.ui.cs.advprog.papikos.payment.repository;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IPaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByFromUserIdOrToUserId(UUID fromUserId, UUID toUserId);
    List<Payment> findByToUserIdEquals(UUID toUserId);
    List<Payment> findByFromUserIdAndStatusOrToUserIdAndStatus(UUID fromUserId, PaymentStatus status1, UUID toUserId, PaymentStatus status2);
}
