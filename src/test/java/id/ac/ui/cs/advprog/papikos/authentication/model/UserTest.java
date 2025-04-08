package id.ac.ui.cs.advprog.papikos.authentication.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserTest {
    private User penyewaUser;
    private User pemilikUser;
    private User adminUser;

    @BeforeEach
    public void setUp() {
        penyewaUser = new User("penyewa@example.com", "p@ssword123", "PENYEWA");
        pemilikUser = new User("pemilik@example.com", "p@ssword456", "PEMILIK_KOS");
        adminUser = new User("admin@example.com", "p@ssword789", "ADMIN");
    }

    @Test
    public void testCreateUserWithEmptyInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("", "", null);
        });
        String expectedMessage = "Semua input tidak boleh kosong!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testCreateUserWithInvalidEmail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("invalid-email", "P@ssword123", "PENYEWA");
        });
        String expectedMessage = "Email tidak valid!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testCreateUserWithWeakPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("user@example.com", "password", "PENYEWA");
        });
        String expectedMessage = "Password harus mengandung kombinasi huruf besar, huruf kecil, angka, dan karakter khusus!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testCreateUsernWithInvalidRole() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("invalid@example.com", "P@ssword456", "INVALID_ROLE");
        });
        String expectedMessage = "Role tidak valid!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testUserCreationPenyewa() {
        assertNotNull(penyewaUser.getId());
        assertEquals("penyewa@example.com", penyewaUser.getEmail());
        assertEquals("PENYEWA", penyewaUser.getRole());
    }

    @Test
    public void testUserCreationPemilikKos() {
        assertNotNull(pemilikUser.getId());
        assertEquals("pemilik@example.com", pemilikUser.getEmail());
        assertEquals("PEMILIK_KOS", pemilikUser.getRole());
    }

    @Test
    public void testUserCreationAdmin() {
        assertNotNull(adminUser.getId());
        assertEquals("admin@example.com", adminUser.getEmail());
        assertEquals("ADMIN", adminUser.getRole());
    }
}
