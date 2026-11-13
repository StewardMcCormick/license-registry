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

public class JdbcLicenseTypeLinkRepository implements LicenseTypeLinkRepository {

    @Override
    public void add(UUID licenseId, UUID typeId) {
        String sql = "INSERT INTO license_type_link (license_id, type_id) VALUES (?, ?)";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, licenseId);
            statement.setObject(2, typeId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось привязать тип к лицензии", e);
        }
    }

    @Override
    public void remove(UUID licenseId, UUID typeId) {
        String sql = "DELETE FROM license_type_link WHERE license_id = ? AND type_id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, licenseId);
            statement.setObject(2, typeId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось отвязать тип от лицензии", e);
        }
    }

    @Override
    public void removeAllByLicenseId(UUID licenseId) {
        String sql = "DELETE FROM license_type_link WHERE license_id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, licenseId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось отвязать типы от лицензии", e);
        }
    }

    @Override
    public List<UUID> findTypeIdsByLicenseId(UUID licenseId) {
        String sql = "SELECT type_id FROM license_type_link WHERE license_id = ?";
        List<UUID> typeIds = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, licenseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    typeIds.add(resultSet.getObject("type_id", UUID.class));
                }
            }
            return typeIds;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить типы лицензии", e);
        }
    }

    @Override
    public List<UUID> findLicenseIdsByTypeId(UUID typeId) {
        String sql = "SELECT license_id FROM license_type_link WHERE type_id = ?";
        List<UUID> licenseIds = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, typeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    licenseIds.add(resultSet.getObject("license_id", UUID.class));
                }
            }
            return licenseIds;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить лицензии по типу", e);
        }
    }
}
