package id.ac.ui.cs.advprog.papikos.payment.controller;

import id.ac.ui.cs.advprog.papikos.authentication.dto.AuthDto;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.payment.dto.PaymentDto;
import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
@Slf4j
public class PaymentRestController {

    private final PaymentService paymentService;
    private final AuthService authService;

    @PostMapping("/topup")
    public ResponseEntity<?> topUp(
            @RequestBody PaymentDto.TopUpRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            if (!user.getId().equals(request.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AuthDto.ApiResponse(false, "Anda hanya dapat top-up ke akun Anda sendiri"));
            }

            paymentService.topUp(request.getUserId(), request.getAmount());
            BigDecimal newBalance = paymentService.getBalance(request.getUserId());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", request.getUserId());
            data.put("amountAdded", request.getAmount());
            data.put("newBalance", newBalance);

            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Top-up berhasil", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/topup-async")
    public ResponseEntity<?> topUpAsync(
            @RequestBody PaymentDto.TopUpRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            if (!user.getId().equals(request.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AuthDto.ApiResponse(false, "Anda hanya dapat top-up ke akun Anda sendiri"));
            }

            paymentService.topUpAsync(request.getUserId(), request.getAmount())
                .thenRun(() -> log.info("Top-up asinkron selesai untuk user: {}, jumlah: {}",
                    request.getUserId(), request.getAmount()))
                .exceptionally(ex -> {
                    log.error("Error pada top-up asinkron untuk user: {}", request.getUserId(), ex);
                    return null;
                });

            Map<String, Object> data = new HashMap<>();
            data.put("userId", request.getUserId());
            data.put("amountRequested", request.getAmount());
            data.put("status", "processing");

            return ResponseEntity.accepted()
                .body(new AuthDto.ApiResponse(true, "Permintaan top-up sedang diproses", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(
            @RequestBody PaymentDto.PaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            if (!user.getId().equals(request.getFromUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AuthDto.ApiResponse(false, "Anda hanya dapat melakukan pembayaran dari akun Anda sendiri"));
            }

            paymentService.pay(request.getFromUserId(), request.getToUserId(), request.getAmount());
            BigDecimal newBalance = paymentService.getBalance(request.getFromUserId());

            Map<String, Object> data = new HashMap<>();
            data.put("fromUserId", request.getFromUserId());
            data.put("toUserId", request.getToUserId());
            data.put("amountPaid", request.getAmount());
            data.put("newBalance", newBalance);

            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Pembayaran berhasil", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/pay-async")
    public ResponseEntity<?> payAsync(
            @RequestBody PaymentDto.PaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            if (!user.getId().equals(request.getFromUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AuthDto.ApiResponse(false, "Anda hanya dapat melakukan pembayaran dari akun Anda sendiri"));
            }

            paymentService.payAsync(request.getFromUserId(), request.getToUserId(), request.getAmount())
                .thenRun(() -> log.info("Pembayaran asinkron selesai dari user: {} ke user: {}, jumlah: {}",
                    request.getFromUserId(), request.getToUserId(), request.getAmount()))
                .exceptionally(ex -> {
                    log.error("Error pada pembayaran asinkron dari user: {} ke user: {}",
                        request.getFromUserId(), request.getToUserId(), ex);
                    return null;
                });

            Map<String, Object> data = new HashMap<>();
            data.put("fromUserId", request.getFromUserId());
            data.put("toUserId", request.getToUserId());
            data.put("amountRequested", request.getAmount());
            data.put("status", "processing");

            return ResponseEntity.accepted()
                .body(new AuthDto.ApiResponse(true, "Permintaan pembayaran sedang diproses", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            BigDecimal balance = paymentService.getBalance(user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("balance", balance);

            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Saldo berhasil diambil", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/balance-async")
    public CompletableFuture<ResponseEntity<?>> getBalanceAsync(@RequestHeader("Authorization") String authHeader) {
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                future.complete(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid")));
                return future;
            }

            // Execute the async operation
            paymentService.getBalanceAsync(user.getId())
                .whenComplete((balance, ex) -> {
                    if (ex != null) {
                        log.error("Error mengambil saldo asinkron untuk user: {}", user.getId(), ex);
                        future.complete(ResponseEntity.badRequest()
                                .body(new AuthDto.ApiResponse(false, ex.getMessage())));
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("userId", user.getId());
                        data.put("balance", balance);

                        future.complete(ResponseEntity.ok(
                                new AuthDto.ApiResponse(true, "Saldo berhasil diambil secara asinkron", data)));
                    }
                });

        } catch (Exception e) {
            log.error("Error in getBalanceAsync", e);
            future.complete(ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage())));
        }

        return future;
    }

    @GetMapping("/wallet")
    public ResponseEntity<?> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String transactionType,
            @RequestHeader("Authorization") String authHeader) {

        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            TransactionType type = null;
            if (transactionType != null && !transactionType.isEmpty()) {
                try {
                    type = TransactionType.valueOf(transactionType);
                } catch (IllegalArgumentException ignored) {
                }
            }

            List<Payment> transactions = paymentService.filterTransactions(
                    user.getId(), startDate, endDate, type);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("transactions", transactions);

            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Transaksi berhasil diambil", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/wallet-async")
    public CompletableFuture<ResponseEntity<?>> getTransactionsAsync(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String transactionType,
            @RequestHeader("Authorization") String authHeader) {

        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                future.complete(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid")));
                return future;
            }

            TransactionType type = null;
            if (transactionType != null && !transactionType.isEmpty()) {
                try {
                    type = TransactionType.valueOf(transactionType);
                } catch (IllegalArgumentException ignored) {
                }
            }

            final TransactionType finalType = type;
            // Execute the async operation
            paymentService.filterTransactionsAsync(user.getId(), startDate, endDate, finalType)
                .whenComplete((transactions, ex) -> {
                    if (ex != null) {
                        log.error("Error mengambil transaksi asinkron untuk user: {}", user.getId(), ex);
                        future.complete(ResponseEntity.badRequest()
                                .body(new AuthDto.ApiResponse(false, ex.getMessage())));
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("userId", user.getId());
                        data.put("transactions", transactions);

                        future.complete(ResponseEntity.ok(
                                new AuthDto.ApiResponse(true, "Transaksi berhasil diambil secara asinkron", data)));
                    }
                });

        } catch (Exception e) {
            log.error("Error in getTransactionsAsync", e);
            future.complete(ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage())));
        }

        return future;
    }

    private User getCurrentUser(String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return null;
            }

            String idStr = authService.decodeToken(token);
            return authService.findById(UUID.fromString(idStr));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

