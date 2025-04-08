package id.ac.ui.cs.advprog.papikos.authentication.model;

import java.util.UUID;
import java.util.regex.Pattern;

public class User {
    private UUID id;
    private String email;
    private String password;
    private String role;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).+$"
    );

    private static final String[] VALID_ROLES = {"PENYEWA", "PEMILIK_KOS", "ADMIN"};

    public User(String email, String password, String role) {
        if (email == null || email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email tidak valid!");
        }
        if (password == null || password.isEmpty() || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password harus mengandung kombinasi huruf, angka, dan karakter khusus!");
        }
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Role harus antara " + String.join(", ", VALID_ROLES));
        }
        this.id = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private boolean isValidRole(String role) {
        for (String validRole : VALID_ROLES) {
            if (validRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}