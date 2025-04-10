package id.ac.ui.cs.advprog.papikos.authentication.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService = AuthServiceImpl.getInstance();

    public static class RegisterRequest {
        public String email;
        public String password;
        public String role;

        public RegisterRequest() {}

        public RegisterRequest(String email, String password, String role) {
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }

    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        Role role = Role.valueOf(request.role);
        User user = authService.registerUser(request.email, request.password, role);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginAuth(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.email, request.password);
            return ResponseEntity.ok(token);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        try {
            authService.logout(token);
            return ResponseEntity.ok("Logout berhasil!");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/approve/{userId}")
    public ResponseEntity<String> approvePemilikKos(@PathVariable UUID userId) {
        try {
            boolean approved = authService.approvePemilikKos(userId);
            if (approved) {
                return ResponseEntity.ok("Akun pemilik kos telah disetujui!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Approval gagal!");
            }
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}