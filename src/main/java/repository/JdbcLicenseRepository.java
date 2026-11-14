package repository;

import config.DatabaseConfig;
import exception.RepositoryException;
import model.License;
import model.LicenseStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcLicenseRepository implements LicenseRepository {

    @Override
    public License create(Connection connection, License license) {
        String sql = "INSERT INTO licenses (org_id, title, issued_at, expires_at, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, license.getOrgId());
            statement.setString(2, license.getTitle());
            statement.setObject(3, license.getIssuedAt());
            statement.setObject(4, license.getExpiresAt());
            statement.setString(5, license.getStatus().name());
            statement.setString(6, license.getNotes());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    license.setId(resultSet.getObject("id", UUID.class));
                    license.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
                    license.setUpdatedAt(resultSet.getObject("updated_at", OffsetDateTime.class));
                }
            }
            return license;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось создать лицензию", e);
        }
    }

    @Override
    public Optional<License> findById(UUID id) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            return findById(connection, id);
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти лицензию по id", e);
        }
    }

    @Override
    public Optional<License> findById(Connection connection, UUID id) {
        String sql = "SELECT * FROM licenses WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти лицензию по id", e);
        }
    }

    @Override
    public List<License> findByOrgId(UUID orgId) {
        String sql = "SELECT * FROM licenses WHERE org_id = ? ORDER BY expires_at";
        List<License> licenses = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, orgId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    licenses.add(mapRow(resultSet));
                }
            }
            return licenses;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить лицензии организации", e);
        }
    }

    @Override
    public List<License> findAll() {
        String sql = "SELECT * FROM licenses ORDER BY expires_at";
        List<License> licenses = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                licenses.add(mapRow(resultSet));
            }
            return licenses;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить список лицензий", e);
        }
    }

    @Override
    public List<License> findAllNonCancelled() {
        String sql = "SELECT * FROM licenses WHERE status <> 'CANCELLED' ORDER BY expires_at";
        List<License> licenses = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                licenses.add(mapRow(resultSet));
            }
            return licenses;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить список лицензий для пересчёта статусов", e);
        }
    }

    @Override
    public List<License> search(UUID orgId, LicenseStatus status, UUID typeId, List<UUID> tagIds, String titleQuery) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT l.* FROM licenses l");
        List<Object> params = new ArrayList<>();

        if (typeId != null) {
            sql.append(" JOIN license_type_link ltl ON ltl.license_id = l.id AND ltl.type_id = ?");
            params.add(typeId);
        }
        if (tagIds != null) {
            int index = 0;
            for (UUID tagId : tagIds) {
                String alias = "lt" + index++;
                sql.append(" JOIN license_tag ").append(alias)
                        .append(" ON ").append(alias).append(".license_id = l.id AND ")
                        .append(alias).append(".tag_id = ?");
                params.add(tagId);
            }
        }

        sql.append(" WHERE l.org_id = ?");
        params.add(orgId);

        if (status != null) {
            sql.append(" AND l.status = ?");
            params.add(status.name());
        }
        if (titleQuery != null && !titleQuery.isBlank()) {
            sql.append(" AND l.title ILIKE ?");
            params.add("%" + titleQuery.trim() + "%");
        }

        sql.append(" ORDER BY l.expires_at");

        List<License> licenses = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    licenses.add(mapRow(resultSet));
                }
            }
            return licenses;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось выполнить поиск лицензий", e);
        }
    }

    @Override
    public void update(Connection connection, License license) {
        String sql = "UPDATE licenses SET title = ?, issued_at = ?, expires_at = ?, status = ?, " +
                "notes = ?, updated_at = now() WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, license.getTitle());
            statement.setObject(2, license.getIssuedAt());
            statement.setObject(3, license.getExpiresAt());
            statement.setString(4, license.getStatus().name());
            statement.setString(5, license.getNotes());
            statement.setObject(6, license.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось обновить лицензию", e);
        }
    }

    @Override
    public void updateStatus(UUID id, LicenseStatus status) {
        String sql = "UPDATE licenses SET status = ?, updated_at = now() WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setObject(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось обновить статус лицензии", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM licenses WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось удалить лицензию", e);
        }
    }

    private License mapRow(ResultSet resultSet) throws SQLException {
        License license = new License();
        license.setId(resultSet.getObject("id", UUID.class));
        license.setOrgId(resultSet.getObject("org_id", UUID.class));
        license.setTitle(resultSet.getString("title"));
        license.setIssuedAt(resultSet.getObject("issued_at", LocalDate.class));
        license.setExpiresAt(resultSet.getObject("expires_at", LocalDate.class));
        license.setStatus(LicenseStatus.valueOf(resultSet.getString("status")));
        license.setNotes(resultSet.getString("notes"));
        license.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
        license.setUpdatedAt(resultSet.getObject("updated_at", OffsetDateTime.class));
        return license;
    }
}
