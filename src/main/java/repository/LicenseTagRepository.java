package repository;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public interface LicenseTagRepository {

    void add(Connection connection, UUID licenseId, UUID tagId);

    void remove(UUID licenseId, UUID tagId);

    void removeAllByLicenseId(Connection connection, UUID licenseId);

    List<UUID> findTagIdsByLicenseId(UUID licenseId);

    List<UUID> findLicenseIdsByTagId(UUID tagId);
}
