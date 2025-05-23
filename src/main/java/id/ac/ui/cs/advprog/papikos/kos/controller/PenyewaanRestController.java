package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.kos.service.PenyewaanService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/penyewaan")
public class PenyewaanRestController {

    private final PenyewaanService penyewaanService;
    private final KosService kosService;
    private final AuthService authService;

    @Autowired
    public PenyewaanRestController(PenyewaanService penyewaanService,
                                   KosService kosService,
                                   AuthService authService) {
        this.penyewaanService = penyewaanService;
        this.kosService = kosService;
        this.authService = authService;
    }

    @GetMapping
    public DeferredResult<ResponseEntity<?>> getAllPenyewaan(@RequestHeader("Authorization") String token) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(30000L);

        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini")));
                return deferredResult;
            }

            penyewaanService.findByPenyewa(user)
                    .whenComplete((penyewaanList, throwable) -> {
                        if (throwable != null) {
                            deferredResult.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(createErrorResponse(throwable.getMessage())));
                        } else {
                            deferredResult.setResult(ResponseEntity.ok(penyewaanList));
                        }
                    });
        } catch (Exception e) {
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage())));
        }

        return deferredResult;
    }

    @GetMapping("/{id}")
    public DeferredResult<ResponseEntity<?>> getPenyewaan(@PathVariable String id,
                                                          @RequestHeader("Authorization") String token) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(30000L);

        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini")));
                return deferredResult;
            }

            penyewaanService.findByIdAndPenyewa(id, user)
                    .whenComplete((optionalPenyewaan, throwable) -> {
                        if (throwable != null) {
                            deferredResult.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(createErrorResponse(throwable.getMessage())));
                        } else if (optionalPenyewaan.isEmpty()) {
                            deferredResult.setResult(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(createErrorResponse("Penyewaan tidak ditemukan")));
                        } else {
                            Penyewaan penyewaan = optionalPenyewaan.get();
                            Map<String, Object> response = new HashMap<>();
                            response.put("penyewaan", penyewaan);
                            response.put("isEditable", penyewaanService.isPenyewaanEditable(penyewaan));
                            response.put("isCancellable", penyewaanService.isPenyewaanCancellable(penyewaan));
                            deferredResult.setResult(ResponseEntity.ok(response));
                        }
                    });
        } catch (Exception e) {
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage())));
        }

        return deferredResult;
    }

    @PostMapping("/kos/{kosId}")
    public DeferredResult<ResponseEntity<?>> createPenyewaan(
            @PathVariable String kosId,
            @Valid @RequestBody Penyewaan penyewaan,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String token) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(30000L);

        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini")));
                return deferredResult;
            }

            penyewaanService.createPenyewaan(penyewaan, kosId, user)
                    .whenComplete((createdPenyewaan, throwable) -> {
                        if (throwable != null) {
                            Throwable cause = throwable.getCause();
                            if (cause instanceof EntityNotFoundException) {
                                deferredResult.setResult(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(createErrorResponse(cause.getMessage())));
                            } else if (cause instanceof IllegalStateException || cause instanceof IllegalArgumentException) {
                                deferredResult.setResult(ResponseEntity.badRequest()
                                        .body(createErrorResponse(cause.getMessage())));
                            } else {
                                deferredResult.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(createErrorResponse(throwable.getMessage())));
                            }
                        } else {
                            deferredResult.setResult(ResponseEntity.status(HttpStatus.CREATED).body(createdPenyewaan));
                        }
                    });
        } catch (Exception e) {
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage())));
        }

        return deferredResult;
    }

    @PutMapping("/{id}")
    public DeferredResult<ResponseEntity<?>> updatePenyewaan(
            @PathVariable String id,
            @Valid @RequestBody Penyewaan updatedPenyewaan,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String token) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(30000L);

        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini")));
                return deferredResult;
            }

            penyewaanService.updatePenyewaan(updatedPenyewaan, id, user)
                    .whenComplete((penyewaan, throwable) -> {
                        if (throwable != null) {
                            Throwable cause = throwable.getCause();
                            if (cause instanceof EntityNotFoundException) {
                                deferredResult.setResult(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(createErrorResponse(cause.getMessage())));
                            } else if (cause instanceof IllegalStateException || cause instanceof IllegalArgumentException) {
                                deferredResult.setResult(ResponseEntity.badRequest()
                                        .body(createErrorResponse(cause.getMessage())));
                            } else {
                                deferredResult.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(createErrorResponse(throwable.getMessage())));
                            }
                        } else {
                            deferredResult.setResult(ResponseEntity.ok(penyewaan));
                        }
                    });
        } catch (Exception e) {
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage())));
        }

        return deferredResult;
    }

    @DeleteMapping("/{id}")
    public DeferredResult<ResponseEntity<?>> cancelPenyewaan(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(30000L);

        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini")));
                return deferredResult;
            }

            penyewaanService.cancelPenyewaan(id, user)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            Throwable cause = throwable.getCause();
                            if (cause instanceof EntityNotFoundException) {
                                deferredResult.setResult(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(createErrorResponse(cause.getMessage())));
                            } else if (cause instanceof IllegalStateException) {
                                deferredResult.setResult(ResponseEntity.badRequest()
                                        .body(createErrorResponse(cause.getMessage())));
                            } else {
                                deferredResult.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(createErrorResponse(throwable.getMessage())));
                            }
                        } else {
                            deferredResult.setResult(ResponseEntity.ok(createSuccessResponse("Penyewaan berhasil dibatalkan")));
                        }
                    });
        } catch (Exception e) {
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage())));
        }

        return deferredResult;
    }

    private User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token tidak valid");
        }

        String token = authHeader.substring(7);
        String idStr = authService.decodeToken(token);
        return authService.findById(UUID.fromString(idStr));
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }
}