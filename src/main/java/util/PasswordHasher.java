package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            byte[] bytes = messageDigest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Алгоритм хэширования " + ALGORITHM + " недоступен", e);
        }
    }

    public static boolean verify(String rawPassword, String storedHash) {
        return hash(rawPassword).equals(storedHash);
    }
}
