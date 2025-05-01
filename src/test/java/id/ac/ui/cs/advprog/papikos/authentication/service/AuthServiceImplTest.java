package id.ac.ui.cs.advprog.papikos.authentication.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    public void testRegisterPenyewa() {
        // Arrange
        String email = "penyewa@example.com";
        String password = "P@ssword123";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            return u;
        });

        User user = authService.registerUser(email, password, Role.PENYEWA);

        assertNotNull(user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(Role.PENYEWA, user.getRole());
        assertTrue(user.isApproved(), "Akun penyewa langsung disetujui!");
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterPemilikKos() {
        String email = "pemilik@example.com";
        String password = "Owner@456!";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            return u;
        });
 
        User user = authService.registerUser(email, password, Role.PEMILIK_KOS);

        assertNotNull(user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(Role.PEMILIK_KOS, user.getRole());
        assertFalse(user.isApproved(), "Akun pemilik kos belum disetujui admin!");
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUserInvalidEmail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("invalid-email", "P@ssword123", Role.PENYEWA);
        });
        String expectedMessage = "Email tidak valid!";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUserWeakPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("user@example.com", "password", Role.PEMILIK_KOS);
        });
        String expectedMessage = "Password harus mengandung kombinasi huruf, angka, dan karakter khusus!";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterDuplicateEmail() {
        String email = "duplicate@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User(email, "P@ssword123", Role.PENYEWA)));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(email, "P@ssword123", Role.PENYEWA);
        });
        String expectedMessage = "Email sudah terdaftar!";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoginSuccess() {
        String email = "login@example.com";
        String password = "P@ssword123";
        User user = new User(email, password, Role.PENYEWA);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String token = authService.login(email, password);
 
        assertNotNull(token, "Token login tidak boleh null!");
        assertTrue(token.startsWith("jwt-"));
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testLoginWrongPassword() {
        String email = "login2@example.com";
        String correctPassword = "P@ssword123";
        String wrongPassword = "WrongP@ss123!";
        User user = new User(email, correctPassword, Role.PENYEWA);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login(email, wrongPassword);
        });
        String expectedMessage = "Username atau password salah!";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testLoginNonExistentUser() {
        String email = "nonexist@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login(email, "Password@789");
        });
        String expectedMessage = "User tidak ditemukan";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testApprovePemilikKosSuccess() {
        UUID userId = UUID.randomUUID();
        User pemilikKos = new User("pemilik@example.com", "Owner@456", Role.PEMILIK_KOS);
        when(userRepository.findById(userId)).thenReturn(Optional.of(pemilikKos));
        when(userRepository.save(any(User.class))).thenReturn(pemilikKos);

        boolean approved = authService.approvePemilikKos(userId);

        assertTrue(approved);
        assertTrue(pemilikKos.isApproved());
        verify(userRepository).findById(userId);
        verify(userRepository).save(pemilikKos);
    }

    @Test
    public void testApprovePemilikKosUserNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.approvePemilikKos(nonExistingId);
        });
        String expectedMessage = "User tidak ditemukan";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository).findById(nonExistingId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testApprovePemilikKosWrongRole() {
        UUID userId = UUID.randomUUID();
        User penyewa = new User("penyewa@example.com", "P@ssword123", Role.PENYEWA);
        when(userRepository.findById(userId)).thenReturn(Optional.of(penyewa));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.approvePemilikKos(userId);
        });
        String expectedMessage = "Hanya akun pemilik kos yang dapat disetujui!";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLogoutAndTokenValidity() {
        String email = "logout@example.com";
        String password = "P@ssword123";
        User user = new User(email, password, Role.PENYEWA);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
 
        String token = authService.login(email, password);
        assertTrue(authService.isTokenValid(token));
        authService.logout(token);
   
        assertFalse(authService.isTokenValid(token));
    }

    @Test
    public void testFailedLogout() {
        String invalidToken = "jwt-invalidtoken";
  
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.logout(invalidToken);
        });
        String expectedMessage = "Token tidak valid atau sudah logout!";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testFindById() {
        UUID userId = UUID.randomUUID();
        User expectedUser = new User("find@example.com", "P@ssword123", Role.PENYEWA);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User foundUser = authService.findById(userId);

        assertNotNull(foundUser);
        assertEquals(expectedUser.getEmail(), foundUser.getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    public void testFindByIdNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.findById(nonExistingId);
        });
        String expectedMessage = "User tidak ditemukan!";
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository).findById(nonExistingId);
    }
}