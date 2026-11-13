package repository;

import model.License;
import model.LicenseStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository {

    License create(License license);

    Optional<License> findById(UUID id);

    List<License> findByOrgId(UUID orgId);

    List<License> findAll();

    List<License> findAllNonCancelled();

    List<License> search(UUID orgId, LicenseStatus status, UUID typeId, UUID tagId, String titleQuery);

    void update(License license);

    void updateStatus(UUID id, LicenseStatus status);

    void delete(UUID id);
}
