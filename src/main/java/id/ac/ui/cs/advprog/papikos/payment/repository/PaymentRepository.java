package id.ac.ui.cs.advprog.papikos.payment.repository;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PaymentRepository implements IPaymentRepository {

    private final List<Payment> payments = new ArrayList<>();

    @Override
    public void save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        boolean duplicate = payments.stream()
                .anyMatch(p -> p.getId().equals(payment.getId()));
        if (duplicate) {
            throw new IllegalArgumentException("Duplicate ID not allowed");
        }

        payments.add(payment);
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(payments);
    }

    @Override
    public List<Payment> findByUserId(UUID userId) {
        return payments.stream()
                .filter(p -> userId.equals(p.getFromUserId()) || userId.equals(p.getToUserId()))
                .collect(Collectors.toList());
    }

    public List<Payment> findByUserIdAndStatus(UUID userId, PaymentStatus status) {
        return payments.stream()
                .filter(p -> status.equals(p.getStatus()) &&
                        (userId.equals(p.getFromUserId()) || userId.equals(p.getToUserId())))
                .collect(Collectors.toList());
    }
}
