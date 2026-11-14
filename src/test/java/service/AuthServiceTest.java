package service;

import exception.ValidationException;
import model.User;
import org.junit.jupiter.api.Test;
import repository.UserRepository;
import util.PasswordHasher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
    private final AuthService authService = new AuthService(userRepository);

    @Test
    void registerRejectsInvalidEmail() {
        assertThrows(ValidationException.class, () ->
                authService.register("not-an-email", "password123", "Иванов", "Иван", null));
    }

    @Test
    void registerRejectsShortPassword() {
        assertThrows(ValidationException.class, () ->
                authService.register("ivan@example.com", "short", "Иванов", "Иван", null));
    }

    @Test
    void registerRejectsBlankName() {
        assertThrows(ValidationException.class, () ->
                authService.register("ivan@example.com", "password123", " ", "Иван", null));
    }

    @Test
    void registerHashesPassword() {
        User user = authService.register("ivan@example.com", "password123", "Иванов", "Иван", null);

        assertNotNull(user.getId());
        assertNotEquals("password123", user.getPasswordHash());
        assertTrue(PasswordHasher.verify("password123", user.getPasswordHash()));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        authService.register("ivan@example.com", "password123", "Иванов", "Иван", null);

        assertThrows(ValidationException.class, () ->
                authService.register("IVAN@example.com", "password123", "Петров", "Пётр", null));
    }

    @Test
    void authenticateReturnsUserForValidCredentials() {
        authService.register("ivan@example.com", "password123", "Иванов", "Иван", null);

        User user = authService.authenticate(" IVAN@example.com ", "password123");

        assertEquals("ivan@example.com", user.getEmail());
    }

    @Test
    void authenticateRejectsWrongPassword() {
        authService.register("ivan@example.com", "password123", "Иванов", "Иван", null);

        assertThrows(ValidationException.class, () ->
                authService.authenticate("ivan@example.com", "wrong-password"));
    }

    private static class InMemoryUserRepository implements UserRepository {

        private final Map<UUID, User> users = new LinkedHashMap<>();

        @Override
        public User create(User user) {
            user.setId(UUID.randomUUID());
            users.put(user.getId(), user);
            return user;
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return users.values().stream()
                    .filter(user -> user.getEmail().equals(email))
                    .findFirst();
        }

        @Override
        public List<User> findByOrganizationId(UUID orgId) {
            return List.of();
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users.values());
        }

        @Override
        public void update(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public void delete(UUID id) {
            users.remove(id);
        }
    }
}