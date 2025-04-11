package id.ac.ui.cs.advprog.papikos.authentication.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthServiceImplTest {
    private AuthService authService;
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository = UserRepository.getInstance();
        userRepository.clear();
        authService = AuthServiceImpl.getInstance();
    }

    @Test
    public void testSingleton() {
        AuthService instance1 = AuthServiceImpl.getInstance();
        AuthService instance2 = AuthServiceImpl.getInstance();
        assertSame(instance1, instance2, "Servis autentikasi seharusnya singleton!");
    }

    @Test
    public void testRegisterPenyewa() {
        User user = authService.registerUser("penyewa@example.com", "P@ssword123", Role.PENYEWA);
        assertNotNull(user.getId());
        assertEquals("penyewa@example.com", user.getEmail());
        assertEquals(Role.PENYEWA, user.getRole());
        assertTrue(user.isApproved(), "Akun penyewa langsung disetujui!");
    }

    @Test
    public void testRegisterPemilikKos() {
        User user = authService.registerUser("pemilik@example.com", "Owner@456!", Role.PEMILIK_KOS);
        assertNotNull(user.getId());
        assertEquals("pemilik@example.com", user.getEmail());
        assertEquals(Role.PEMILIK_KOS, user.getRole());
        assertFalse(user.isApproved(), "Akun pemilik kos belum disetujui admin!");
    }

    @Test
    public void testRegisterUserInvalidEmail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("invalid-email", "P@ssword123", Role.PENYEWA);
        });
        String expectedMessage = "Email tidak valid!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testRegisterUserWeakPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("user@example.com", "password", Role.PEMILIK_KOS);
        });
        String expectedMessage = "Password harus mengandung kombinasi huruf, angka, dan karakter khusus!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testLoginSuccess() {
        authService.registerUser("login@example.com", "P@ssword123", Role.PENYEWA);
        String token = authService.login("login@example.com", "P@ssword123");
        assertNotNull(token, "Token login tidak boleh null!");
        assertTrue(token.contains("-"));
    }

    @Test
    public void testLoginWrongInput() {
        authService.registerUser("login2@example.com", "P@ssword123", Role.PENYEWA);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("login1@example.com", "WrongP@ss123!");
        });
        String expectedMessage = "Username atau password salah!";
        assertFalse(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testLoginNonExistentUser() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("nonexist@example.com", "Password@789");
        });
        String expectedMessage = "User tidak ditemukan!";
        assertFalse(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testApprovePemilikKosSuccess() {
        User pemilikKos = authService.registerUser("pemilik@example.com", "Owner@456", Role.PEMILIK_KOS);
        assertFalse(pemilikKos.isApproved());
        boolean approved = authService.approvePemilikKos(pemilikKos.getId());
        assertTrue(approved);
        User updated = userRepository.findById(pemilikKos.getId());
        assertTrue(updated.isApproved());
    }

    @Test
    public void testApprovePemilikKosUserNotFound() {
        UUID nonExisting = UUID.randomUUID();
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.approvePemilikKos(nonExisting);
        });
        String expectedMessage = "User tidak ditemukan";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testApprovePemilikKosWrongRole() {
        User penyewa = authService.registerUser("penyewa@example.com", "P@ssword123", Role.PENYEWA);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.approvePemilikKos(penyewa.getId());
        });
        String expectedMessage = "Hanya akun pemilik kos yang dapat disetujui!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testLogoutAndTokenValidity() {
        authService.registerUser("login@example.com", "P@ssword123", Role.PENYEWA);
        String token = authService.login("login@example.com", "P@ssword123");
        assertTrue(((AuthServiceImpl) authService).isTokenValid(token));
        authService.logout(token);
        assertFalse(((AuthServiceImpl) authService).isTokenValid(token));
    }

    @Test
    public void testFailedLogout() {
        authService.registerUser("failed@example.com", "P@ssword123", Role.PENYEWA);
        String token = authService.login("failed@example.com", "P@ssword123");
        String modifiedToken = token.substring(0, token.length() - 1)
                + (token.charAt(token.length() - 1) != 'a' ? 'a' : 'b');
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.logout(modifiedToken);
        });
        String expectedMessage = "Token tidak valid atau sudah logout!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testDecodeToken() {
        User user = authService.registerUser("decode@example.com", "P@ssword123", Role.PENYEWA);
        String token = authService.login("decode@example.com", "P@ssword123");
        String decodedId = ((AuthServiceImpl) authService).decodeToken(token);
        assertEquals(user.getId().toString(), decodedId,
                "Token yang di-decode harus menghasilkan UUID user yang sama!");
    }
}
