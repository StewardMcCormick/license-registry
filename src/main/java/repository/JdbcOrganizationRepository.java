package repository;

import config.DatabaseConfig;
import exception.RepositoryException;
import model.Organization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcOrganizationRepository implements OrganizationRepository {

    @Override
    public Organization create(Connection connection, Organization organization) {
        String sql = "INSERT INTO organizations (name, join_code) VALUES (?, ?) RETURNING id, created_at";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, organization.getName());
            statement.setString(2, organization.getJoinCode());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    organization.setId(resultSet.getObject("id", UUID.class));
                    organization.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
                }
            }
            return organization;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось создать организацию", e);
        }
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        String sql = "SELECT * FROM organizations WHERE id = ?";
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
            throw new RepositoryException("Не удалось найти организацию по id", e);
        }
    }

    @Override
    public Optional<Organization> findByJoinCode(String joinCode) {
        String sql = "SELECT * FROM organizations WHERE join_code = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, joinCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти организацию по join_code", e);
        }
    }

    @Override
    public List<Organization> findAll() {
        String sql = "SELECT * FROM organizations ORDER BY created_at";
        List<Organization> organizations = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                organizations.add(mapRow(resultSet));
            }
            return organizations;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить список организаций", e);
        }
    }

    @Override
    public void update(Organization organization) {
        String sql = "UPDATE organizations SET name = ?, join_code = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, organization.getName());
            statement.setString(2, organization.getJoinCode());
            statement.setObject(3, organization.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось обновить организацию", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM organizations WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось удалить организацию", e);
        }
    }

    @Override
    public List<Organization> getByUserId(UUID userId) {
        String sql = """
                SELECT o.id, o.name, o.join_code, o.created_at
                FROM organizations o JOIN organization_member om
                    ON o.id = om.org_id
                WHERE om.user_id = ?
                """;
        List<Organization> organizations = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    organizations.add(mapRow(resultSet));
                }
            }
            return organizations;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось загрузить список организаций", e);
        }
    }

    private Organization mapRow(ResultSet resultSet) throws SQLException {
        Organization organization = new Organization();
        organization.setId(resultSet.getObject("id", UUID.class));
        organization.setName(resultSet.getString("name"));
        organization.setJoinCode(resultSet.getString("join_code"));
        organization.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
        return organization;
    }
}
