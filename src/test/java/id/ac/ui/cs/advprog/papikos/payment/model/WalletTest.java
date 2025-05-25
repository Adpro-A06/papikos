package id.ac.ui.cs.advprog.papikos.payment.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private Wallet wallet;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        wallet = new Wallet(userId);
    }

    @Test
    void testWalletConstructor() {
        assertEquals(userId, wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void testAddBalance() {
        BigDecimal initialBalance = wallet.getBalance();
        BigDecimal amountToAdd = new BigDecimal("100.50");

        wallet.addBalance(amountToAdd);

        assertEquals(initialBalance.add(amountToAdd), wallet.getBalance());
    }

    @Test
    void testAddZeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.addBalance(BigDecimal.ZERO);
        });
    }

    @Test
    void testAddNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.addBalance(new BigDecimal("-50"));
        });
    }

    @Test
    void testSubtractBalance() {
        // First add some balance
        BigDecimal amountToAdd = new BigDecimal("200");
        wallet.addBalance(amountToAdd);

        BigDecimal amountToSubtract = new BigDecimal("50.50");
        wallet.subtractBalance(amountToSubtract);

        assertEquals(amountToAdd.subtract(amountToSubtract), wallet.getBalance());
    }

    @Test
    void testSubtractZeroAmount() {
        wallet.addBalance(new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> {
            wallet.subtractBalance(BigDecimal.ZERO);
        });
    }

    @Test
    void testSubtractNegativeAmount() {
        wallet.addBalance(new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> {
            wallet.subtractBalance(new BigDecimal("-50"));
        });
    }

    @Test
    void testSubtractInsufficientBalance() {
        wallet.addBalance(new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> {
            wallet.subtractBalance(new BigDecimal("150"));
        });

        // Confirm balance remains unchanged
        assertEquals(new BigDecimal("100"), wallet.getBalance());
    }

    @Test
    void testSubtractExactBalance() {
        BigDecimal amount = new BigDecimal("100");
        wallet.addBalance(amount);

        wallet.subtractBalance(amount);

        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void testSetters() {
        UUID newUserId = UUID.randomUUID();
        BigDecimal newBalance = new BigDecimal("500");

        wallet.setUserId(newUserId);
        wallet.setBalance(newBalance);

        assertEquals(newUserId, wallet.getUserId());
        assertEquals(newBalance, wallet.getBalance());
    }
}