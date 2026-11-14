package repository;

import model.License;
import model.LicenseStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository {

    License create(Connection connection, License license);

    Optional<License> findById(UUID id);

    Optional<License> findById(Connection connection, UUID id);

    List<License> findByOrgId(UUID orgId);

    List<License> findAll();

    List<License> findAllNonCancelled();

    List<License> search(UUID orgId, LicenseStatus status, UUID typeId, List<UUID> tagIds, String titleQuery);

    void update(Connection connection, License license);

    void updateStatus(UUID id, LicenseStatus status);

    void delete(UUID id);
}
