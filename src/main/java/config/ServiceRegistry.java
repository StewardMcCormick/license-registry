package config;

import repository.JdbcLicenseRepository;
import repository.JdbcLicenseTagRepository;
import repository.JdbcLicenseTypeLinkRepository;
import repository.JdbcLicenseTypeRepository;
import repository.JdbcOrganizationMemberRepository;
import repository.JdbcOrganizationRepository;
import repository.JdbcTagRepository;
import repository.JdbcUserRepository;
import repository.LicenseRepository;
import repository.LicenseTagRepository;
import repository.LicenseTypeLinkRepository;
import repository.LicenseTypeRepository;
import repository.OrganizationMemberRepository;
import repository.OrganizationRepository;
import repository.TagRepository;
import repository.UserRepository;
import service.AdminService;
import service.AuthService;
import service.LicenseService;
import service.LicenseTypeService;
import service.OrganizationService;
import service.TagService;

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
