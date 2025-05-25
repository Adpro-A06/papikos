package id.ac.ui.cs.advprog.papikos.payment.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class PaymentLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(PaymentLoggingAspect.class);

    @Around("execution(* id.ac.ui.cs.advprog.papikos.payment.service.PaymentService.topUp(..))")
    public Object logTopUp(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        UUID userId = (UUID) args[0];
        BigDecimal amount = (BigDecimal) args[1];

        log.info("Processing top-up request for user {} with amount {}", userId, amount);

        try {
            Object result = joinPoint.proceed();
            log.info("Top-up completed successfully for user {}", userId);
            return result;
        } catch (Exception e) {
            log.error("Error during top-up for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Around("execution(* id.ac.ui.cs.advprog.papikos.payment.service.PaymentService.pay(..))")
    public Object logPayment(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        UUID fromUserId = (UUID) args[0];
        UUID toUserId = (UUID) args[1];
        BigDecimal amount = (BigDecimal) args[2];

        log.info("Processing payment from user {} to user {} with amount {}", fromUserId, toUserId, amount);

        try {
            Object result = joinPoint.proceed();
            log.info("Payment completed successfully from user {} to user {}", fromUserId, toUserId);
            return result;
        } catch (Exception e) {
            log.error("Error during payment from user {} to user {}: {}", fromUserId, toUserId, e.getMessage(), e);
            throw e;
        }
    }

    @AfterThrowing(pointcut = "execution(* id.ac.ui.cs.advprog.papikos.payment.service.*.*(..))", throwing = "exception")
    public void logServiceException(JoinPoint joinPoint, Exception exception) {
        log.error("Exception in {} with args {} - Exception: {}",
                joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs()),
                exception.getMessage());
    }
}
