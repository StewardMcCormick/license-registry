package service;

import exception.NotFoundException;
import exception.RepositoryException;
import exception.ValidationException;
import model.LicenseType;
import repository.LicenseTypeRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class LicenseTypeService {

    private static final String FOREIGN_KEY_VIOLATION_SQL_STATE = "23503";

    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseTypeService(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    public LicenseType create(String code, String name) {
        String normalizedCode = normalize(code);
        String normalizedName = normalizeName(name);
        if (licenseTypeRepository.findByCode(normalizedCode).isPresent()) {
            throw new ValidationException("Тип лицензии с таким кодом уже существует");
        }
        return licenseTypeRepository.create(new LicenseType(normalizedCode, normalizedName));
    }

    public LicenseType getById(UUID id) {
        return licenseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тип лицензии не найден"));
    }

    public List<LicenseType> getAll() {
        return licenseTypeRepository.findAll();
    }

    public void rename(UUID id, String newCode, String newName) {
        LicenseType licenseType = getById(id);
        String normalizedCode = normalize(newCode);
        String normalizedName = normalizeName(newName);
        licenseTypeRepository.findByCode(normalizedCode).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ValidationException("Тип лицензии с таким кодом уже существует");
            }
        });
        licenseType.setCode(normalizedCode);
        licenseType.setName(normalizedName);
        licenseTypeRepository.update(licenseType);
    }

    public void delete(UUID id) {
        getById(id);
        try {
            licenseTypeRepository.delete(id);
        } catch (RepositoryException e) {
            if (e.getCause() instanceof SQLException sqlException
                    && FOREIGN_KEY_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                throw new ValidationException("Тип лицензии используется и не может быть удалён");
            }
            throw e;
        }
    }

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Код типа лицензии обязателен");
        }
        return code.trim().toUpperCase();
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название типа лицензии обязательно");
        }
        return name.trim();
    }
}
