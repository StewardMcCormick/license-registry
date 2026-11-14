package service;

import exception.NotFoundException;
import exception.ValidationException;
import model.License;
import model.LicenseStatus;
import model.LicenseType;
import model.Tag;
import org.junit.jupiter.api.Test;
import repository.LicenseRepository;
import repository.LicenseTagRepository;
import repository.LicenseTypeLinkRepository;
import repository.LicenseTypeRepository;
import repository.TagRepository;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LicenseServiceTest {

    private final StubLicenseRepository licenseRepository = new StubLicenseRepository();
    private final StubTagRepository tagRepository = new StubTagRepository();
    private final LicenseService licenseService = new LicenseService(
            licenseRepository,
            new StubLicenseTypeRepository(),
            new StubLicenseTypeLinkRepository(),
            tagRepository,
            new StubLicenseTagRepository(),
            new TagService(tagRepository)
    );

    @Test
    void createRejectsBlankTitle() {
        assertThrows(ValidationException.class, () -> licenseService.create(
                UUID.randomUUID(), "  ", LocalDate.now(), LocalDate.now().plusDays(10),
                null, List.of(), List.of()));
    }

    @Test
    void createRejectsMissingDates() {
        assertThrows(ValidationException.class, () -> licenseService.create(
                UUID.randomUUID(), "Лицензия", null, LocalDate.now(),
                null, List.of(), List.of()));
    }

    @Test
    void createRejectsIssuedDateAfterExpiryDate() {
        assertThrows(ValidationException.class, () -> licenseService.create(
                UUID.randomUUID(), "Лицензия", LocalDate.now().plusDays(2), LocalDate.now(),
                null, List.of(), List.of()));
    }

    @Test
    void updateRejectsBlankTitleBeforeTouchingDatabase() {
        assertThrows(ValidationException.class, () -> licenseService.update(
                UUID.randomUUID(), " ", LocalDate.now(), LocalDate.now().plusDays(1),
                null, List.of(), List.of()));
    }

    @Test
    void updateRejectsIssuedDateAfterExpiryDate() {
        assertThrows(ValidationException.class, () -> licenseService.update(
                UUID.randomUUID(), "Лицензия", LocalDate.now().plusDays(2), LocalDate.now(),
                null, List.of(), List.of()));
    }

    @Test
    void getByIdThrowsNotFoundWhenLicenseDoesNotExist() {
        assertThrows(NotFoundException.class, () -> licenseService.getById(UUID.randomUUID()));
    }

    @Test
    void searchDelegatesToRepository() {
        List<License> result = licenseService.search(
                UUID.randomUUID(), LicenseStatus.ACTIVE, null, null, "тест");

        assertSame(licenseRepository.lastSearchResult, result);
    }

    private static class StubLicenseRepository implements LicenseRepository {

        private List<License> lastSearchResult = new ArrayList<>();

        @Override
        public License create(Connection connection, License license) {
            return license;
        }

        @Override
        public Optional<License> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<License> findById(Connection connection, UUID id) {
            return Optional.empty();
        }

        @Override
        public List<License> findByOrgId(UUID orgId) {
            return List.of();
        }

        @Override
        public List<License> findAll() {
            return List.of();
        }

        @Override
        public List<License> findAllNonCancelled() {
            return List.of();
        }

        @Override
        public List<License> search(UUID orgId, LicenseStatus status, UUID typeId, List<UUID> tagIds, String titleQuery) {
            lastSearchResult = new ArrayList<>();
            return lastSearchResult;
        }

        @Override
        public void update(Connection connection, License license) {
        }

        @Override
        public void updateStatus(UUID id, LicenseStatus status) {
        }

        @Override
        public void delete(UUID id) {
        }
    }

    private static class StubLicenseTypeRepository implements LicenseTypeRepository {

        @Override
        public LicenseType create(LicenseType licenseType) {
            return licenseType;
        }

        @Override
        public Optional<LicenseType> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<LicenseType> findById(Connection connection, UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<LicenseType> findByCode(String code) {
            return Optional.empty();
        }

        @Override
        public List<LicenseType> findAll() {
            return List.of();
        }

        @Override
        public void update(LicenseType licenseType) {
        }

        @Override
        public void delete(UUID id) {
        }
    }

    private static class StubLicenseTypeLinkRepository implements LicenseTypeLinkRepository {

        @Override
        public void add(Connection connection, UUID licenseId, UUID typeId) {
        }

        @Override
        public void remove(UUID licenseId, UUID typeId) {
        }

        @Override
        public void removeAllByLicenseId(Connection connection, UUID licenseId) {
        }

        @Override
        public List<UUID> findTypeIdsByLicenseId(UUID licenseId) {
            return List.of();
        }

        @Override
        public List<UUID> findLicenseIdsByTypeId(UUID typeId) {
            return List.of();
        }
    }

    private static class StubTagRepository implements TagRepository {

        @Override
        public Tag create(Connection connection, Tag tag) {
            return tag;
        }

        @Override
        public Optional<Tag> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<Tag> findByOrgIdAndName(UUID orgId, String name) {
            return Optional.empty();
        }

        @Override
        public Optional<Tag> findByOrgIdAndName(Connection connection, UUID orgId, String name) {
            return Optional.empty();
        }

        @Override
        public List<Tag> findByOrgId(UUID orgId) {
            return List.of();
        }

        @Override
        public void update(Tag tag) {
        }

        @Override
        public void delete(UUID id) {
        }
    }

    private static class StubLicenseTagRepository implements LicenseTagRepository {

        @Override
        public void add(Connection connection, UUID licenseId, UUID tagId) {
        }

        @Override
        public void remove(UUID licenseId, UUID tagId) {
        }

        @Override
        public void removeAllByLicenseId(Connection connection, UUID licenseId) {
        }

        @Override
        public List<UUID> findTagIdsByLicenseId(UUID licenseId) {
            return List.of();
        }

        @Override
        public List<UUID> findLicenseIdsByTagId(UUID tagId) {
            return List.of();
        }
    }
}