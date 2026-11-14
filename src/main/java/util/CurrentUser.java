package util;

import model.GlobalRole;
import model.User;

import java.io.Serializable;
import java.util.UUID;

public class CurrentUser implements Serializable {

    private final UUID userId;
    private final String email;
    private final String fullName;
    private final GlobalRole globalRole;

    public CurrentUser(UUID userId, String email, String fullName, GlobalRole globalRole) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.globalRole = globalRole;
    }

    public static CurrentUser of(User user) {
        return new CurrentUser(user.getId(), user.getEmail(),
                user.getLastName() + " " + user.getFirstName(), user.getGlobalRole());
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public GlobalRole getGlobalRole() {
        return globalRole;
    }

    public boolean isSuperadmin() {
        return globalRole == GlobalRole.SUPERADMIN;
    }
}
