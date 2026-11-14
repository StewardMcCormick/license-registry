package service;

import exception.NotFoundException;
import exception.ValidationException;
import model.GlobalRole;
import model.License;
import model.LicenseStatus;
import model.MemberRole;
import model.Organization;
import model.OrganizationMember;
import model.User;
import repository.LicenseRepository;
import repository.OrganizationMemberRepository;
import repository.OrganizationRepository;
import repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Сервис администрирования: агрегирует данные для панели суперадмина
 * (общая статистика и сводки по организациям) и управляет глобальной ролью
 * пользователей ({@code grantSuperadmin}/{@code revokeSuperadmin}).
 */
public class AdminService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final LicenseRepository licenseRepository;

    public AdminService(UserRepository userRepository, OrganizationRepository organizationRepository,
                         OrganizationMemberRepository memberRepository, LicenseRepository licenseRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.licenseRepository = licenseRepository;
    }

    public GlobalStats getGlobalStats() {
        List<License> licenses = licenseRepository.findAll();
        long expiringCount = 0;
        long expiredCount = 0;
        for (License license : licenses) {
            if (license.getStatus() == LicenseStatus.EXPIRING) {
                expiringCount++;
            } else if (license.getStatus() == LicenseStatus.EXPIRED) {
                expiredCount++;
            }
        }
        return new GlobalStats(
                userRepository.findAll().size(),
                organizationRepository.findAll().size(),
                licenses.size(),
                expiringCount,
                expiredCount
        );
    }

    public List<OrganizationSummary> getOrganizationSummaries() {
        List<OrganizationSummary> summaries = new ArrayList<>();
        for (Organization organization : organizationRepository.findAll()) {
            summaries.add(toSummary(organization));
        }
        return summaries;
    }

    public void grantSuperadmin(UUID userId) {
        setGlobalRole(userId, GlobalRole.SUPERADMIN);
    }

    public void revokeSuperadmin(UUID userId) {
        User user = requireUser(userId);
        if (user.getGlobalRole() == GlobalRole.SUPERADMIN && countSuperadmins() <= 1) {
            throw new ValidationException("Нельзя снять роль с последнего суперадмина");
        }
        user.setGlobalRole(GlobalRole.USER);
        userRepository.update(user);
    }

    private void setGlobalRole(UUID userId, GlobalRole globalRole) {
        User user = requireUser(userId);
        user.setGlobalRole(globalRole);
        userRepository.update(user);
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private long countSuperadmins() {
        return userRepository.findAll().stream()
                .filter(user -> user.getGlobalRole() == GlobalRole.SUPERADMIN)
                .count();
    }

    private OrganizationSummary toSummary(Organization organization) {
        List<OrganizationMember> members = memberRepository.findByOrgId(organization.getId());
        long adminCount = 0;
        long managerCount = 0;
        for (OrganizationMember member : members) {
            if (member.getMemberRole() == MemberRole.ADMIN) {
                adminCount++;
            } else if (member.getMemberRole() == MemberRole.MANAGER) {
                managerCount++;
            }
        }
        long licenseCount = licenseRepository.findByOrgId(organization.getId()).size();
        return new OrganizationSummary(organization, adminCount, managerCount, licenseCount);
    }

    public static class GlobalStats {

        private final long userCount;
        private final long organizationCount;
        private final long licenseCount;
        private final long expiringCount;
        private final long expiredCount;

        public GlobalStats(long userCount, long organizationCount, long licenseCount,
                            long expiringCount, long expiredCount) {
            this.userCount = userCount;
            this.organizationCount = organizationCount;
            this.licenseCount = licenseCount;
            this.expiringCount = expiringCount;
            this.expiredCount = expiredCount;
        }

        public long getUserCount() {
            return userCount;
        }

        public long getOrganizationCount() {
            return organizationCount;
        }

        public long getLicenseCount() {
            return licenseCount;
        }

        public long getExpiringCount() {
            return expiringCount;
        }

        public long getExpiredCount() {
            return expiredCount;
        }
    }

    public static class OrganizationSummary {

        private final Organization organization;
        private final long adminCount;
        private final long managerCount;
        private final long licenseCount;

        public OrganizationSummary(Organization organization, long adminCount, long managerCount, long licenseCount) {
            this.organization = organization;
            this.adminCount = adminCount;
            this.managerCount = managerCount;
            this.licenseCount = licenseCount;
        }

        public Organization getOrganization() {
            return organization;
        }

        public long getAdminCount() {
            return adminCount;
        }

        public long getManagerCount() {
            return managerCount;
        }

        public long getLicenseCount() {
            return licenseCount;
        }
    }
}
