package id.ac.ui.cs.advprog.papikos.authentication.repository;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class UserRepository {
    private static UserRepository instance;
    private Map<UUID, User> userById;
    private Map<String, User> userByEmail;

    private UserRepository() {
        userById = new HashMap<>();
        userByEmail = new HashMap<>();
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public void save(User user) {
        userById.put(user.getId(), user);
        userByEmail.put(user.getEmail(), user);
    }

    public User findById(UUID id) {
        return userById.get(id);
    }

    public User findByEmail(String email) {
        return userByEmail.get(email);
    }

    public void clear() {
        userById.clear();
        userByEmail.clear();
    }
}
