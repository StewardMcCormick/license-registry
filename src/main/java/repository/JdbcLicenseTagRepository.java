package repository;

import config.DatabaseConfig;
import exception.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcLicenseTagRepository implements LicenseTagRepository {

    @Override
    public void add(Connection connection, UUID licenseId, UUID tagId) {
        String sql = "INSERT INTO license_tag (license_id, tag_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, licenseId);
            statement.setObject(2, tagId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось привязать тег к лицензии", e);
        }
    }

    @Override
    public void remove(UUID licenseId, UUID tagId) {
        String sql = "DELETE FROM license_tag WHERE license_id = ? AND tag_id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, licenseId);
            statement.setObject(2, tagId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось отвязать тег от лицензии", e);
        }
    }

    @Override
    public void removeAllByLicenseId(Connection connection, UUID licenseId) {
        String sql = "DELETE FROM license_tag WHERE license_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, licenseId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось отвязать теги от лицензии", e);
        }
    }

    @Override
    public List<UUID> findTagIdsByLicenseId(UUID licenseId) {
        String sql = "SELECT tag_id FROM license_tag WHERE license_id = ?";
        List<UUID> tagIds = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, licenseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tagIds.add(resultSet.getObject("tag_id", UUID.class));
                }
            }
            return tagIds;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить теги лицензии", e);
        }
    }

    @Override
    public List<UUID> findLicenseIdsByTagId(UUID tagId) {
        String sql = "SELECT license_id FROM license_tag WHERE tag_id = ?";
        List<UUID> licenseIds = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, tagId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    licenseIds.add(resultSet.getObject("license_id", UUID.class));
                }
            }
            return licenseIds;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить лицензии по тегу", e);
        }
    }
}
