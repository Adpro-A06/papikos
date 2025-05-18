package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/kos")
public class KosRestController {

    private final KosService kosService;
    private final AuthService authService;

    @Autowired
    public KosRestController(KosService kosService, AuthService authService) {
        this.kosService = kosService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getAllKos(@RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Anda harus login terlebih dahulu"));
            }

            List<Kos> availableKos = kosService.findAllAvailable();
            return ResponseEntity.ok(availableKos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchKos(@RequestParam(required = false) String keyword,
                                      @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Anda harus login terlebih dahulu"));
            }

            List<Kos> searchResults;
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchResults = kosService.searchByKeyword(keyword);
            } else {
                searchResults = kosService.findAllAvailable();
            }
            
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getKosDetail(@PathVariable String id,
                                        @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Anda harus login terlebih dahulu"));
            }

            try {
                UUID kosUUID = UUID.fromString(id);
                return kosService.findById(kosUUID)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .<Kos>body(null));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Format ID kos tidak valid"));
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
}