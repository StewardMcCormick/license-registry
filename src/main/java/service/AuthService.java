package service;

import exception.ValidationException;
import model.GlobalRole;
import model.User;
import repository.UserRepository;
import util.PasswordHasher;

import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String email, String rawPassword, String lastName, String firstName, String middleName) {
        String normalizedEmail = normalizeEmail(email);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new ValidationException("Некорректный email");
        }
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Пароль должен быть не короче " + MIN_PASSWORD_LENGTH + " символов");
        }
        if (lastName == null || lastName.isBlank() || firstName == null || firstName.isBlank()) {
            throw new ValidationException("Фамилия и имя обязательны");
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        String middleNameTrimmed = (middleName == null || middleName.isBlank()) ? null : middleName.trim();
        User user = new User(normalizedEmail, PasswordHasher.hash(rawPassword), lastName.trim(),
                firstName.trim(), middleNameTrimmed, GlobalRole.USER);
        return userRepository.create(user);
    }

    public User authenticate(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ValidationException("Неверный email или пароль"));
        if (!PasswordHasher.verify(rawPassword, user.getPasswordHash())) {
            throw new ValidationException("Неверный email или пароль");
        }
        return user;
    }

    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!PasswordHasher.verify(oldPassword, user.getPasswordHash())) {
            throw new ValidationException("Старый пароль указан неверно");
        }
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Пароль должен быть не короче " + MIN_PASSWORD_LENGTH + " символов");
        }
        user.setPasswordHash(PasswordHasher.hash(newPassword));
        userRepository.update(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
