package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    private static final String CURRENT_USER_ATTRIBUTE = "currentUser";

    private SessionUtil() {
    }

    public static void login(HttpServletRequest request, CurrentUser user) {
        request.getSession(true).setAttribute(CURRENT_USER_ATTRIBUTE, user);
    }

    public static CurrentUser getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (CurrentUser) session.getAttribute(CURRENT_USER_ATTRIBUTE);
    }

    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
