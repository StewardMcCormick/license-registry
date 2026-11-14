package repository.license.type;

import model.license.type.LicenseType;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseTypeRepository {

    LicenseType create(LicenseType licenseType);

    Optional<LicenseType> findById(UUID id);

    Optional<LicenseType> findById(Connection connection, UUID id);

    Optional<LicenseType> findByOrgIdAndCode(UUID orgId, String code);

    List<LicenseType> findByOrgId(UUID orgId);

    void update(LicenseType licenseType);

    void delete(UUID id);
}
