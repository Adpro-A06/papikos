package id.ac.ui.cs.advprog.papikos.authentication.repository;

import static org.junit.jupiter.api.Assertions.*;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

@DataJpaTest
public class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private User testUser;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        testUser = new User("test@example.com", "P@ssword963", Role.PENYEWA);
    }

    @Test
    public void testSaveUser() {
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        assertNotNull(savedUser.getId(), "User ID tidak boleh null setelah disimpan");

        Optional<User> foundUser = userRepository.findByEmail(testUser.getEmail());
        assertTrue(foundUser.isPresent(), "User berhasil disimpan dan ditemukan berdasarkan email!");
        assertEquals(savedUser.getId(), foundUser.get().getId());
    }

    @Test
    public void testFindUserById() {
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent(), "User ditemukan berdasarkan id!");
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    public void testFindUserNotExists() {
        Optional<User> foundUser = userRepository.findByEmail("nonexist@example.com");
        assertFalse(foundUser.isPresent(), "User tidak ditemukan dengan email tersebut!");
    }
    
    @Test
    public void testDeleteUser() {
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        userRepository.delete(savedUser);
        entityManager.flush();
   
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertFalse(foundUser.isPresent(), "User berhasil dihapus!");
    }
    
    @Test
    public void testExistsById() {
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        boolean exists = userRepository.existsById(savedUser.getId());
        assertTrue(exists, "User ditemukan dengan existsById!");
    }
}