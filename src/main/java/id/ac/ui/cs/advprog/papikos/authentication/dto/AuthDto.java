package id.ac.ui.cs.advprog.papikos.authentication.dto;

import java.util.UUID;
import lombok.Data;

public class AuthDto {
    
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }
    
    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String role;
    }
    
    @Data
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;
        
        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }
    
    @Data
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private UUID userId;
        private String email;
        private String role;
        
        public JwtResponse(String token, UUID userId, String email, String role) {
            this.token = token;
            this.userId = userId;
            this.email = email;
            this.role = role;
        }
    }
}