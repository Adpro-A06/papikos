package id.ac.ui.cs.advprog.papikos.authentication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Getter
    private UUID id;

    @Getter
    @Column(unique = true, nullable = false)
    private String email;

    @Getter
    @Column(nullable = false)
    private String password;

    @Getter
    @Enumerated(EnumType.STRING)
    private Role role;

    @Getter @Setter
    private boolean approved;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).+$"
    );

    private static final String[] VALID_ROLES = {"PENYEWA", "PEMILIK_KOS", "ADMIN"};

    public User() {}

    public User(String email, String password, Role role) {
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
        this.approved = !role.equals(Role.PEMILIK_KOS);
    }

    private boolean isValidRole(Role role) {
        for (String validRole : VALID_ROLES) {
            if (validRole.equals(role.name())) {
                return true;
            }
        }
        return false;
    }
}