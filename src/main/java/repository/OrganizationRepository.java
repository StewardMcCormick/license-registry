package repository;

import model.Organization;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository {

    Organization create(Connection connection, Organization organization);

    Optional<Organization> findById(UUID id);

    Optional<Organization> findByJoinCode(String joinCode);

    List<Organization> findAll();

    List<Organization> getByUserId(UUID userId);

    void update(Organization organization);

    void delete(UUID id);
}
