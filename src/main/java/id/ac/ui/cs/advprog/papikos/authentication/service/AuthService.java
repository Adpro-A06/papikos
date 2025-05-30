package id.ac.ui.cs.advprog.papikos.authentication.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;

import java.util.List;
import java.util.UUID;

public interface AuthService {
    User registerUser(String email, String password, Role role);
    String login(String email, String password);
    void logout(String token);
    boolean approvePemilikKos(UUID userId);
    List<User> findAllPendingPemilikKos();
    User findById(UUID userId);
    String decodeToken(String token);
    List<User> findAllPemilikKos();
}

