package util;

import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;

public class CsrfTokenUtil {

    private static final String SESSION_ATTRIBUTE = "csrfToken";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokenUtil() {
    }

    public static String getOrCreateToken(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_ATTRIBUTE);
        if (token == null) {
            token = generate();
            session.setAttribute(SESSION_ATTRIBUTE, token);
        }
        return token;
    }

    public static boolean isValid(HttpSession session, String submittedToken) {
        String token = (String) session.getAttribute(SESSION_ATTRIBUTE);
        return token != null && token.equals(submittedToken);
    }

    private static String generate() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return token.toString();
    }
}
