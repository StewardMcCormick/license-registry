package config;

import repository.license.JdbcLicenseRepository;
import repository.license.JdbcLicenseTagRepository;
import repository.license.JdbcLicenseTypeLinkRepository;
import repository.license.type.JdbcLicenseTypeRepository;
import repository.organization.JdbcOrganizationMemberRepository;
import repository.organization.JdbcOrganizationRepository;
import repository.tag.JdbcTagRepository;
import repository.user.JdbcUserRepository;
import repository.license.LicenseRepository;
import repository.license.LicenseTagRepository;
import repository.license.LicenseTypeLinkRepository;
import repository.license.type.LicenseTypeRepository;
import repository.organization.OrganizationMemberRepository;
import repository.organization.OrganizationRepository;
import repository.tag.TagRepository;
import repository.user.UserRepository;
import service.admin.AdminService;
import service.auth.AuthService;
import service.license.LicenseService;
import service.license.type.LicenseTypeService;
import service.organization.OrganizationService;
import service.tag.TagService;

public class ServiceRegistry {

    public static final String CONTEXT_ATTRIBUTE = "serviceRegistry";

    private final AuthService authService;
    private final OrganizationService organizationService;
    private final LicenseTypeService licenseTypeService;
    private final TagService tagService;
    private final LicenseService licenseService;
    private final AdminService adminService;

    public ServiceRegistry() {
        UserRepository userRepository = new JdbcUserRepository();
        OrganizationRepository organizationRepository = new JdbcOrganizationRepository();
        OrganizationMemberRepository memberRepository = new JdbcOrganizationMemberRepository();
        LicenseTypeRepository licenseTypeRepository = new JdbcLicenseTypeRepository();
        LicenseRepository licenseRepository = new JdbcLicenseRepository();
        TagRepository tagRepository = new JdbcTagRepository();
        LicenseTypeLinkRepository licenseTypeLinkRepository = new JdbcLicenseTypeLinkRepository();
        LicenseTagRepository licenseTagRepository = new JdbcLicenseTagRepository();

        this.authService = new AuthService(userRepository);
        this.organizationService = new OrganizationService(organizationRepository, memberRepository);
        this.tagService = new TagService(tagRepository);
        this.licenseTypeService = new LicenseTypeService(licenseTypeRepository);
        this.licenseService = new LicenseService(licenseRepository, licenseTypeRepository,
                licenseTypeLinkRepository, tagRepository, licenseTagRepository, tagService);
        this.adminService = new AdminService(userRepository, organizationRepository, memberRepository,
                licenseRepository);
    }

    public AuthService getAuthService() {
        return authService;
    }

    public OrganizationService getOrganizationService() {
        return organizationService;
    }

    public LicenseTypeService getLicenseTypeService() {
        return licenseTypeService;
    }

    public TagService getTagService() {
        return tagService;
    }

    public LicenseService getLicenseService() {
        return licenseService;
    }

    public AdminService getAdminService() {
        return adminService;
    }
}
