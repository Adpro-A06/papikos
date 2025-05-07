package id.ac.ui.cs.advprog.papikos.authentication.controller;

import id.ac.ui.cs.advprog.papikos.authentication.dto.AuthDto;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final AuthService authService;

    @Autowired
    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDto.RegisterRequest request) {
        try {
            Role role = Role.valueOf(request.getRole());
            User user = authService.registerUser(request.getEmail(), request.getPassword(), role);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthDto.ApiResponse(true, "Registrasi berhasil!",
                            Map.of("email", user.getEmail(), "role", user.getRole().name())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request) {
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            String idStr = authService.decodeToken(token);
            User user = authService.findById(UUID.fromString(idStr));

            return ResponseEntity.ok(new AuthDto.JwtResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthDto.ApiResponse(false, ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token != null) {
                authService.logout(token);
                return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Logout berhasil"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthDto.ApiResponse(false, "Error saat logout: " + ex.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            String idStr = authService.decodeToken(token);
            User user = authService.findById(UUID.fromString(idStr));

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            userData.put("approved", user.isApproved());

            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Data user berhasil diambil", userData));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthDto.ApiResponse(false, ex.getMessage()));
        }
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}