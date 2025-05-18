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

import java.util.HashMap;
import java.util.List;
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
    public ResponseEntity<?> getAllPenyewaan(@RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini"));
            }

            List<Penyewaan> penyewaanList = penyewaanService.findByPenyewa(user);
            return ResponseEntity.ok(penyewaanList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPenyewaan(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini"));
            }

            return penyewaanService.findByIdAndPenyewa(id, user)
                    .map(penyewaan -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("penyewaan", penyewaan);
                        response.put("isEditable", penyewaanService.isPenyewaanEditable(penyewaan));
                        response.put("isCancellable", penyewaanService.isPenyewaanCancellable(penyewaan));
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(createErrorResponse("Penyewaan tidak ditemukan")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/kos/{kosId}")
    public ResponseEntity<?> createPenyewaan(@PathVariable String kosId,
            @Valid @RequestBody Penyewaan penyewaan,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini"));
            }

            try {
                Penyewaan createdPenyewaan = penyewaanService.createPenyewaan(penyewaan, kosId, user);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdPenyewaan);
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(e.getMessage()));
            } catch (IllegalStateException | IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePenyewaan(@PathVariable String id,
            @Valid @RequestBody Penyewaan updatedPenyewaan,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini"));
            }

            try {
                Penyewaan penyewaan = penyewaanService.updatePenyewaan(updatedPenyewaan, id, user);
                return ResponseEntity.ok(penyewaan);
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(e.getMessage()));
            } catch (IllegalStateException | IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelPenyewaan(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            if (user == null || user.getRole() != Role.PENYEWA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Anda tidak memiliki akses ke resource ini"));
            }

            try {
                penyewaanService.cancelPenyewaan(id, user);
                return ResponseEntity.ok(createSuccessResponse("Penyewaan berhasil dibatalkan"));
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(e.getMessage()));
            } catch (IllegalStateException e) {
                return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        }
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