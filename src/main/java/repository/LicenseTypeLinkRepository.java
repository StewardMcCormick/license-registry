package repository;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public interface LicenseTypeLinkRepository {

    void add(Connection connection, UUID licenseId, UUID typeId);

    void remove(UUID licenseId, UUID typeId);

    void removeAllByLicenseId(Connection connection, UUID licenseId);

    List<UUID> findTypeIdsByLicenseId(UUID licenseId);

    List<UUID> findLicenseIdsByTypeId(UUID typeId);
}
