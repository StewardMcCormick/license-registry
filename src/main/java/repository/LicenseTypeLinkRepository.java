package repository;

import java.util.List;
import java.util.UUID;

public interface LicenseTypeLinkRepository {

    void add(UUID licenseId, UUID typeId);

    void remove(UUID licenseId, UUID typeId);

    void removeAllByLicenseId(UUID licenseId);

    List<UUID> findTypeIdsByLicenseId(UUID licenseId);

    List<UUID> findLicenseIdsByTypeId(UUID typeId);
}
