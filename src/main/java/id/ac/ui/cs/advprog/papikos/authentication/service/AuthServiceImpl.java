package id.ac.ui.cs.advprog.papikos.authentication.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).+$"
    );

    private Set<String> validTokens = new HashSet<>();

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(String email, String password, Role role) {
        if (email == null || email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email tidak valid!");
        }
        if (password == null || password.isEmpty() || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password harus mengandung kombinasi huruf, angka, dan karakter khusus!");
        }
  
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email sudah terdaftar!");
        }
        
        User user = new User(email, password, role);
        return userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User tidak ditemukan");
        }
        
        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Username atau password salah!");
        }
        
        String token = generateToken(user);
        validTokens.add(token);
        return token;
    }

    @Override
    public void logout(String token) {
        if (!validTokens.remove(token)) {
            throw new RuntimeException("Token tidak valid atau sudah logout!");
        }
    }

    @Override
    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
    }

    @Override
    public boolean approvePemilikKos(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User tidak ditemukan");
        }
        
        User user = userOpt.get();
        if (user.getRole() != Role.PEMILIK_KOS) {
            throw new RuntimeException("Hanya akun pemilik kos yang dapat disetujui!");
        }
        
        user.setApproved(true);
        userRepository.save(user);
        return true;
    }

    private String generateToken(User user) {
        String idStr = user.getId().toString();
        String shifted = caesarCipher(idStr, 7);
        return "jwt-" + shifted;
    }

    private String caesarCipher(String input, int shift) {
        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isDigit(ch)) {
                char shiftedChar = (char)(((ch - '0' + shift) % 10) + '0');
                result.append(shiftedChar);
            } else if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                char shiftedChar = (char)(((ch - base + shift) % 26) + base);
                result.append(shiftedChar);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public String decodeToken(String token) {
        if (token == null || !token.startsWith("jwt-")) {
            throw new IllegalArgumentException("Token tidak valid!");
        }
        String withoutPrefix = token.substring(4);
        return reverseCaesarCipher(withoutPrefix, 7);
    }

    private String reverseCaesarCipher(String input, int shift) {
        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isDigit(ch)) {
                int shiftedValue = ch - '0' - shift;
                if (shiftedValue < 0) {
                    shiftedValue += 10;
                }
                result.append((char)(shiftedValue + '0'));
            } else if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int shiftedValue = ch - base - shift;
                if (shiftedValue < 0) {
                    shiftedValue += 26;
                }
                result.append((char)(shiftedValue + base));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public boolean isTokenValid(String token) {
        return validTokens.contains(token);
    }
}