package repository.util;

import model.GlobalRole;
import model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

public class RowMapper {

    private RowMapper() {
    }

    public static User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getObject("id", UUID.class));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setLastName(resultSet.getString("last_name"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setMiddleName(resultSet.getString("middle_name"));
        user.setGlobalRole(GlobalRole.valueOf(resultSet.getString("global_role")));
        user.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
        return user;
    }
}
