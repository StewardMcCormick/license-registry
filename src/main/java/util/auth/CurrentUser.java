package util.auth;

import model.user.GlobalRole;
import model.user.User;

import java.io.Serializable;
import java.util.UUID;

public record CurrentUser(UUID userId, String email, String fullName, GlobalRole globalRole) implements Serializable {

    public static CurrentUser of(User user) {
        return new CurrentUser(user.getId(), user.getEmail(),
                user.getLastName() + " " + user.getFirstName() +
                        (user.getMiddleName() == null || user.getMiddleName().isEmpty() ? "" : " " + user.getMiddleName()),
                user.getGlobalRole());
    }

    public boolean isSuperadmin() {
        return globalRole == GlobalRole.SUPERADMIN;
    }
}
