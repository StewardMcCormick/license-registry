package repository;

import model.LicenseType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseTypeRepository {

    LicenseType create(LicenseType licenseType);

    Optional<LicenseType> findById(UUID id);

    Optional<LicenseType> findByCode(String code);

    List<LicenseType> findAll();

    void update(LicenseType licenseType);

    void delete(UUID id);
}
