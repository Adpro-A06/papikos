package id.ac.ui.cs.advprog.papikos.payment.repository;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;

import java.util.List;
import java.util.UUID;

public interface IPaymentRepository {
    void save(Payment payment);
    List<Payment> findAll();
    List<Payment> findByUserId(UUID userId);
}
