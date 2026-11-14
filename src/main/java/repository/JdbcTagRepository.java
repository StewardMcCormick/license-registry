package repository;

import config.DatabaseConfig;
import exception.RepositoryException;
import model.Tag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcTagRepository implements TagRepository {

    @Override
    public Tag create(Connection connection, Tag tag) {
        String sql = "INSERT INTO tags (org_id, name) VALUES (?, ?) " +
                "ON CONFLICT (org_id, name) DO UPDATE SET name = EXCLUDED.name RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, tag.getOrgId());
            statement.setString(2, tag.getName());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    tag.setId(resultSet.getObject("id", UUID.class));
                }
            }
            return tag;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось создать тег", e);
        }
    }

    @Override
    public Optional<Tag> findById(UUID id) {
        String sql = "SELECT * FROM tags WHERE id = ?";
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
            throw new RepositoryException("Не удалось найти тег по id", e);
        }
    }

    @Override
    public Optional<Tag> findByOrgIdAndName(UUID orgId, String name) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            return findByOrgIdAndName(connection, orgId, name);
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти тег по имени", e);
        }
    }

    @Override
    public Optional<Tag> findByOrgIdAndName(Connection connection, UUID orgId, String name) {
        String sql = "SELECT * FROM tags WHERE org_id = ? AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, orgId);
            statement.setString(2, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось найти тег по имени", e);
        }
    }

    @Override
    public List<Tag> findByOrgId(UUID orgId) {
        String sql = "SELECT * FROM tags WHERE org_id = ? ORDER BY name";
        List<Tag> tags = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, orgId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tags.add(mapRow(resultSet));
                }
            }
            return tags;
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось получить теги организации", e);
        }
    }

    @Override
    public void update(Tag tag) {
        String sql = "UPDATE tags SET name = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tag.getName());
            statement.setObject(2, tag.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось обновить тег", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM tags WHERE id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Не удалось удалить тег", e);
        }
    }

    private Tag mapRow(ResultSet resultSet) throws SQLException {
        Tag tag = new Tag();
        tag.setId(resultSet.getObject("id", UUID.class));
        tag.setOrgId(resultSet.getObject("org_id", UUID.class));
        tag.setName(resultSet.getString("name"));
        return tag;
    }
}
