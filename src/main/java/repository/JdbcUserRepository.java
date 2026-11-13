package repository;

import config.DatabaseConfig;
import exception.RepositoryException;
import model.GlobalRole;
import model.User;
import repository.util.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserRepository implements UserRepository {

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, password_hash, last_name, first_name, middle_name, global_role) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getMiddleName());
            statement.setString(6, user.getGlobalRole().name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getObject("id", UUID.class));
                    user.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось создать пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        String sql = "SELECT * FROM users u WHERE u.id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(RowMapper.mapUser(resultSet, ""));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти пользователя по id", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users u WHERE u.email = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(RowMapper.mapUser(resultSet, ""));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти пользователя по email", e);
        }
    }

    @Override
    public List<User> findByOrganizationId(UUID orgId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN organization_member om ON om.user_id = u.id " +
                "WHERE om.org_id = ? ORDER BY u.created_at";
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, orgId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(RowMapper.mapUser(resultSet, ""));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить пользователей организации", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users u ORDER BY u.created_at";
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(RowMapper.mapUser(resultSet, ""));
            }
            return users;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить список пользователей", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET email = ?, password_hash = ?, last_name = ?, first_name = ?, " +
                "middle_name = ?, global_role = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getMiddleName());
            statement.setString(6, user.getGlobalRole().name());
            statement.setObject(7, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось обновить пользователя", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM users u WHERE u.id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось удалить пользователя", e);
        }
    }
}
