package service;

import exception.RepositoryException;
import exception.ValidationException;
import model.GlobalRole;
import model.User;
import repository.UserRepository;
import util.PasswordHasher;

import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_NAME_LENGTH = 100;
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String email, String rawPassword, String lastName, String firstName, String middleName) {
        String normalizedEmail = normalizeEmail(email);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new ValidationException("Некорректный email");
        }
        if (normalizedEmail.length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException("Email слишком длинный");
        }
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Пароль должен быть не короче " + MIN_PASSWORD_LENGTH + " символов");
        }
        String lastNameTrimmed = normalizeName(lastName, "Фамилия");
        String firstNameTrimmed = normalizeName(firstName, "Имя");
        String middleNameTrimmed = normalizeOptionalName(middleName);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        User user = new User(normalizedEmail, PasswordHasher.hash(rawPassword), lastNameTrimmed,
                firstNameTrimmed, middleNameTrimmed, GlobalRole.USER);
        try {
            return userRepository.create(user);
        } catch (RepositoryException e) {
            if (e.hasSqlState(UNIQUE_VIOLATION_SQL_STATE)) {
                throw new ValidationException("Пользователь с таким email уже существует");
            }
            throw e;
        }
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

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new ValidationException(fieldName + " обязательно");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new ValidationException(fieldName + " не должно превышать " + MAX_NAME_LENGTH + " символов");
        }
        return trimmed;
    }

    private String normalizeOptionalName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Отчество не должно превышать " + MAX_NAME_LENGTH + " символов");
        }
        return trimmed;
    }
}
