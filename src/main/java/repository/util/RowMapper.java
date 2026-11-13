package repository.util;

import model.GlobalRole;
import model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

public class RowMapper {

	public static User mapUser(ResultSet resultSet, String prefix) throws SQLException {
		User user = new User();
		user.setId(resultSet.getObject(getRowNameWithPrefix(prefix, "id"), UUID.class));
		user.setEmail(resultSet.getString(getRowNameWithPrefix(prefix,"email")));
		user.setPasswordHash(resultSet.getString(getRowNameWithPrefix(prefix,"password_hash")));
		user.setLastName(resultSet.getString(getRowNameWithPrefix(prefix,"last_name")));
		user.setFirstName(resultSet.getString(getRowNameWithPrefix(prefix,"first_name")));
		user.setMiddleName(resultSet.getString(getRowNameWithPrefix(prefix,"middle_name")));
		user.setGlobalRole(GlobalRole.valueOf(resultSet.getString(getRowNameWithPrefix(prefix,"global_role"))));
		user.setCreatedAt(resultSet.getObject(getRowNameWithPrefix(prefix,"created_at"), OffsetDateTime.class));
		return user;
	}

	private static String getRowNameWithPrefix(String prefix, String rowName) {
		return String.format("%s%s", prefix == null || prefix.isEmpty() ? "" : prefix + ".", rowName);
	}
}
