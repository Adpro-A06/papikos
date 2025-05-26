package id.ac.ui.cs.advprog.papikos.payment.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class PaymentMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter topUpCounter;
    private final Counter paymentCounter;
    private final Counter failedPaymentCounter;
    private final Timer paymentProcessingTimer;
    private final ConcurrentHashMap<String, Timer> operationTimers = new ConcurrentHashMap<>();

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.topUpCounter = Counter.builder("papikos.payment.topup.count")
                .description("Number of top-up operations")
                .register(meterRegistry);

        this.paymentCounter = Counter.builder("papikos.payment.transaction.count")
                .description("Number of payment transactions")
                .register(meterRegistry);

        this.failedPaymentCounter = Counter.builder("papikos.payment.transaction.failed")
                .description("Number of failed payment transactions")
                .register(meterRegistry);

        this.paymentProcessingTimer = Timer.builder("papikos.payment.processing.time")
                .description("Time taken to process payments")
                .register(meterRegistry);
    }

    public void recordTopUp(BigDecimal amount) {
        topUpCounter.increment();
        meterRegistry.gauge("papikos.payment.topup.amount", amount.doubleValue());
    }

    public void recordPayment(BigDecimal amount) {
        paymentCounter.increment();
        meterRegistry.gauge("papikos.payment.transaction.amount", amount.doubleValue());
    }

    public void recordFailedPayment() {
        failedPaymentCounter.increment();
    }

    public Timer.Sample startPaymentProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPaymentProcessingTimer(Timer.Sample sample) {
        sample.stop(paymentProcessingTimer);
    }

    public void recordOperationTime(String operation, long timeInMs) {
        operationTimers.computeIfAbsent(operation,
            k -> Timer.builder("papikos.payment.operation.time")
                .tag("operation", operation)
                .description("Time taken for payment operations")
                .register(meterRegistry))
            .record(timeInMs, TimeUnit.MILLISECONDS);
    }
}
