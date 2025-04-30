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
        penyewaUser = new User("penyewa@example.com", "p@ssword123", Role.PENYEWA);
        pemilikUser = new User("pemilik@example.com", "p@ssword456", Role.PEMILIK_KOS);
        adminUser = new User("admin@example.com", "p@ssword789", Role.ADMIN);
    }

    @Test
    public void testCreateUserWithEmptyInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("", "", null);
        });
        String expectedMessage = "Semua input tidak boleh kosong!";
        assertFalse(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testCreateUserWithInvalidEmail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("invalid-email", "P@ssword123", Role.PENYEWA);
        });
        String expectedMessage = "Email tidak valid!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testCreateUserWithWeakPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new User("user@example.com", "password", Role.PENYEWA);
        });
        String expectedMessage = "Password harus mengandung kombinasi huruf, angka, dan karakter khusus!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testCreateUserWithInvalidRole() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Role invalidRole = Role.valueOf("INVALID_ROLE");
            new User("invalid@example.com", "P@ssword456", invalidRole);
        });
        String expectedMessage = "Role harus antara PENYEWA, PEMILIK_KOS, dan ADMIN!";
        assertFalse(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testCreateUserPenyewa() {
        assertNotNull(penyewaUser.getId());
        assertEquals("penyewa@example.com", penyewaUser.getEmail());
        assertEquals(Role.PENYEWA, penyewaUser.getRole());
    }

    @Test
    public void testCreateUserPemilikKos() {
        assertNotNull(pemilikUser.getId());
        assertEquals("pemilik@example.com", pemilikUser.getEmail());
        assertEquals(Role.PEMILIK_KOS, pemilikUser.getRole());
    }

    @Test
    public void testCreateUserAdmin() {
        assertNotNull(adminUser.getId());
        assertEquals("admin@example.com", adminUser.getEmail());
        assertEquals(Role.ADMIN, adminUser.getRole());
    }
}
