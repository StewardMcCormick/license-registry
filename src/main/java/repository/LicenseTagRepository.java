package repository;

import java.util.List;
import java.util.UUID;

public interface LicenseTagRepository {

    void add(UUID licenseId, UUID tagId);

    void remove(UUID licenseId, UUID tagId);

    void removeAllByLicenseId(UUID licenseId);

    List<UUID> findTagIdsByLicenseId(UUID licenseId);

    List<UUID> findLicenseIdsByTagId(UUID tagId);
}
