package repository;

import model.Tag;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository {

    Tag create(Connection connection, Tag tag);

    Optional<Tag> findById(UUID id);

    Optional<Tag> findByOrgIdAndName(UUID orgId, String name);

    Optional<Tag> findByOrgIdAndName(Connection connection, UUID orgId, String name);

    List<Tag> findByOrgId(UUID orgId);

    void update(Tag tag);

    void delete(UUID id);
}
