package repository;

import config.DatabaseConfig;
import exception.RepositoryException;
import model.LicenseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcLicenseTypeRepository implements LicenseTypeRepository {

    @Override
    public LicenseType create(LicenseType licenseType) {
        String sql = "INSERT INTO license_types (code, name) VALUES (?, ?) RETURNING id";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, licenseType.getCode());
            statement.setString(2, licenseType.getName());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    licenseType.setId(resultSet.getObject("id", UUID.class));
                }
            }
            return licenseType;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось создать тип лицензии", e);
        }
    }

    @Override
    public Optional<LicenseType> findById(UUID id) {
        String sql = "SELECT * FROM license_types WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти тип лицензии по id", e);
        }
    }

    @Override
    public Optional<LicenseType> findByCode(String code) {
        String sql = "SELECT * FROM license_types WHERE code = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти тип лицензии по коду", e);
        }
    }

    @Override
    public List<LicenseType> findAll() {
        String sql = "SELECT * FROM license_types ORDER BY code";
        List<LicenseType> licenseTypes = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                licenseTypes.add(mapRow(resultSet));
            }
            return licenseTypes;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить список типов лицензий", e);
        }
    }

    @Override
    public void update(LicenseType licenseType) {
        String sql = "UPDATE license_types SET code = ?, name = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, licenseType.getCode());
            statement.setString(2, licenseType.getName());
            statement.setObject(3, licenseType.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось обновить тип лицензии", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM license_types WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось удалить тип лицензии", e);
        }
    }

    private LicenseType mapRow(ResultSet resultSet) throws SQLException {
        LicenseType licenseType = new LicenseType();
        licenseType.setId(resultSet.getObject("id", UUID.class));
        licenseType.setCode(resultSet.getString("code"));
        licenseType.setName(resultSet.getString("name"));
        return licenseType;
    }
}
