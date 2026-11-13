package service;

import config.AppConfig;
import exception.NotFoundException;
import exception.ValidationException;
import model.License;
import model.LicenseStatus;
import model.LicenseType;
import model.Tag;
import repository.LicenseRepository;
import repository.LicenseTagRepository;
import repository.LicenseTypeLinkRepository;
import repository.LicenseTypeRepository;
import repository.TagRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseTypeLinkRepository licenseTypeLinkRepository;
    private final TagRepository tagRepository;
    private final LicenseTagRepository licenseTagRepository;
    private final TagService tagService;

    public LicenseService(LicenseRepository licenseRepository, LicenseTypeRepository licenseTypeRepository,
                           LicenseTypeLinkRepository licenseTypeLinkRepository, TagRepository tagRepository,
                           LicenseTagRepository licenseTagRepository, TagService tagService) {
        this.licenseRepository = licenseRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.licenseTypeLinkRepository = licenseTypeLinkRepository;
        this.tagRepository = tagRepository;
        this.licenseTagRepository = licenseTagRepository;
        this.tagService = tagService;
    }

    public License create(UUID orgId, String title, LocalDate issuedAt, LocalDate expiresAt, String notes,
                           List<UUID> typeIds, List<String> tagNames) {
        validateTitle(title);
        validateDates(issuedAt, expiresAt);

        LicenseStatus status = computeStatus(expiresAt);
        License license = new License(orgId, title.trim(), issuedAt, expiresAt, status, normalizeNotes(notes));
        license = licenseRepository.create(license);

        linkTypes(license.getId(), typeIds);
        linkTags(license.getId(), orgId, tagNames);

        return license;
    }

    public License update(UUID licenseId, String title, LocalDate issuedAt, LocalDate expiresAt, String notes,
                           List<UUID> typeIds, List<String> tagNames) {
        License license = getById(licenseId);

        validateTitle(title);
        validateDates(issuedAt, expiresAt);

        license.setTitle(title.trim());
        license.setIssuedAt(issuedAt);
        license.setExpiresAt(expiresAt);
        license.setNotes(normalizeNotes(notes));
        if (license.getStatus() != LicenseStatus.CANCELLED) {
            license.setStatus(computeStatus(expiresAt));
        }

        licenseRepository.update(license);

        licenseTypeLinkRepository.removeAllByLicenseId(licenseId);
        linkTypes(licenseId, typeIds);

        licenseTagRepository.removeAllByLicenseId(licenseId);
        linkTags(licenseId, license.getOrgId(), tagNames);

        return license;
    }

    public void cancel(UUID licenseId) {
        getById(licenseId);
        licenseRepository.updateStatus(licenseId, LicenseStatus.CANCELLED);
    }

    public void restore(UUID licenseId) {
        License license = getById(licenseId);
        if (license.getStatus() != LicenseStatus.CANCELLED) {
            return;
        }
        licenseRepository.updateStatus(licenseId, computeStatus(license.getExpiresAt()));
    }

    public void delete(UUID licenseId) {
        getById(licenseId);
        licenseRepository.delete(licenseId);
    }

    public License getById(UUID licenseId) {
        return licenseRepository.findById(licenseId)
                .orElseThrow(() -> new NotFoundException("Лицензия не найдена"));
    }

    public List<License> getByOrgId(UUID orgId) {
        return licenseRepository.findByOrgId(orgId);
    }

    public List<License> search(UUID orgId, LicenseStatus status, UUID typeId, UUID tagId, String titleQuery) {
        return licenseRepository.search(orgId, status, typeId, tagId, titleQuery);
    }

    public List<LicenseType> getTypes(UUID licenseId) {
        List<LicenseType> types = new ArrayList<>();
        for (UUID typeId : licenseTypeLinkRepository.findTypeIdsByLicenseId(licenseId)) {
            licenseTypeRepository.findById(typeId).ifPresent(types::add);
        }
        return types;
    }

    public List<Tag> getTags(UUID licenseId) {
        List<Tag> tags = new ArrayList<>();
        for (UUID tagId : licenseTagRepository.findTagIdsByLicenseId(licenseId)) {
            tagRepository.findById(tagId).ifPresent(tags::add);
        }
        return tags;
    }

    public int recalculateStatuses() {
        int updatedCount = 0;
        for (License license : licenseRepository.findAllNonCancelled()) {
            LicenseStatus newStatus = computeStatus(license.getExpiresAt());
            if (newStatus != license.getStatus()) {
                licenseRepository.updateStatus(license.getId(), newStatus);
                updatedCount++;
            }
        }
        return updatedCount;
    }

    private void linkTypes(UUID licenseId, List<UUID> typeIds) {
        if (typeIds == null) {
            return;
        }
        Set<UUID> distinctTypeIds = new LinkedHashSet<>(typeIds);
        for (UUID typeId : distinctTypeIds) {
            if (licenseTypeRepository.findById(typeId).isEmpty()) {
                throw new ValidationException("Тип лицензии не найден: " + typeId);
            }
            licenseTypeLinkRepository.add(licenseId, typeId);
        }
    }

    private void linkTags(UUID licenseId, UUID orgId, List<String> tagNames) {
        if (tagNames == null) {
            return;
        }
        Set<String> distinctNames = new LinkedHashSet<>();
        for (String name : tagNames) {
            if (name != null && !name.isBlank()) {
                distinctNames.add(name.trim());
            }
        }
        for (String name : distinctNames) {
            Tag tag = tagService.getOrCreate(orgId, name);
            licenseTagRepository.add(licenseId, tag.getId());
        }
    }

    private LicenseStatus computeStatus(LocalDate expiresAt) {
        LocalDate today = LocalDate.now();
        if (expiresAt.isBefore(today)) {
            return LicenseStatus.EXPIRED;
        }
        LocalDate expiringBoundary = today.plusDays(AppConfig.EXPIRING_THRESHOLD_DAYS);
        if (!expiresAt.isAfter(expiringBoundary)) {
            return LicenseStatus.EXPIRING;
        }
        return LicenseStatus.ACTIVE;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Название лицензии обязательно");
        }
    }

    private void validateDates(LocalDate issuedAt, LocalDate expiresAt) {
        if (issuedAt == null || expiresAt == null) {
            throw new ValidationException("Дата выдачи и дата истечения обязательны");
        }
        if (issuedAt.isAfter(expiresAt)) {
            throw new ValidationException("Дата выдачи не может быть позже даты истечения");
        }
    }

    private String normalizeNotes(String notes) {
        return (notes == null || notes.isBlank()) ? null : notes.trim();
    }
}
