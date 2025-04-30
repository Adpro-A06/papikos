package id.ac.ui.cs.advprog.papikos.authentication.repository;

import static org.junit.jupiter.api.Assertions.*;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserRepositoryTest {
    private UserRepository userRepository;
    private User testUser;

    @BeforeEach
    public void setUp() {
        userRepository = UserRepository.getInstance();
        userRepository.clear();
        testUser = new User("test@example.com", "P@ssword963", Role.PENYEWA);
    }

    @Test
    public void testSingleton() {
        UserRepository instance1 = UserRepository.getInstance();
        UserRepository instance2 = UserRepository.getInstance();
        assertSame(instance1, instance2, "Seharusnya menggunakan satu instance (singleton)!");
    }

    @Test
    public void testSaveUser() {
        userRepository.save(testUser);
        User found = userRepository.findByEmail(testUser.getEmail());
        assertNotNull(found, "User berhasil disimpan dan ditemukan berdasarkan email!");
        assertEquals(testUser.getId(), found.getId());
    }

    @Test
    public void testFindUserById() {
        userRepository.save(testUser);
        User found = userRepository.findById(testUser.getId());
        assertNotNull(found, "User ditemukan berdasarkan id!");
        assertEquals(testUser.getEmail(), found.getEmail());
    }

    @Test
    public void testFindUserNotExists() {
        User found = userRepository.findByEmail("nonexist@example.com");
        assertNull(found, "User tidak ditemukan dengan email tersebut!");
    }
}
