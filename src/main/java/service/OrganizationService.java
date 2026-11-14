package service;

import config.DatabaseConfig;
import exception.NotFoundException;
import exception.RepositoryException;
import exception.ValidationException;
import model.MemberRole;
import model.Organization;
import model.OrganizationMember;
import repository.OrganizationMemberRepository;
import repository.OrganizationRepository;
import util.JoinCodeGenerator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrganizationService {

    private static final int MAX_JOIN_CODE_ATTEMPTS = 10;
    private static final int MAX_NAME_LENGTH = 255;
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                                OrganizationMemberRepository memberRepository) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
    }

    public Organization createOrganization(String name, UUID creatorUserId) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название организации обязательно");
        }
        String normalizedName = name.trim();
        if (normalizedName.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Название организации не должно превышать " + MAX_NAME_LENGTH + " символов");
        }
        Organization organization = new Organization(normalizedName, generateUniqueJoinCode());
        return DatabaseConfig.inTransaction(connection -> {
            Organization created = organizationRepository.create(connection, organization);
            memberRepository.create(connection,
                    new OrganizationMember(created.getId(), creatorUserId, MemberRole.ADMIN));
            return created;
        });
    }

    public Organization joinOrganization(String joinCode, UUID userId) {
        if (joinCode == null || joinCode.isBlank()) {
            throw new ValidationException("Join-код обязателен");
        }
        Organization organization = organizationRepository.findByJoinCode(joinCode.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Организация с таким join-кодом не найдена"));
        if (memberRepository.findByOrgAndUser(organization.getId(), userId).isPresent()) {
            throw new ValidationException("Пользователь уже состоит в этой организации");
        }
        try {
            memberRepository.create(new OrganizationMember(organization.getId(), userId, MemberRole.MANAGER));
        } catch (RepositoryException e) {
            if (e.hasSqlState(UNIQUE_VIOLATION_SQL_STATE)) {
                throw new ValidationException("Пользователь уже состоит в этой организации");
            }
            throw e;
        }
        return organization;
    }

    public String reissueJoinCode(UUID orgId) {
        Organization organization = getById(orgId);
        organization.setJoinCode(generateUniqueJoinCode());
        organizationRepository.update(organization);
        return organization.getJoinCode();
    }

    public void promoteToAdmin(UUID orgId, UUID userId) {
        OrganizationMember member = memberRepository.findByOrgAndUser(orgId, userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не состоит в организации"));
        if (member.getMemberRole() == MemberRole.ADMIN) {
            return;
        }
        memberRepository.updateRole(orgId, userId, MemberRole.ADMIN);
    }

    public void lowerToManager(UUID orgId, UUID userId) {
        OrganizationMember member = memberRepository.findByOrgAndUser(orgId, userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не состоит в организации"));
        if (member.getMemberRole() == MemberRole.MANAGER) {
            return;
        }
        if (countAdmins(orgId) <= 1) {
            throw new ValidationException("В организации должен остаться хотя бы один администратор");
        }
        memberRepository.updateRole(orgId, userId, MemberRole.MANAGER);
    }

    public Organization getById(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Организация не найдена"));
    }

    public List<Organization> getAll() {
        return organizationRepository.findAll();
    }

    public List<Organization> getByUserId(UUID userId) {
        return organizationRepository.getByUserId(userId);
    }

    public List<OrganizationMember> getMembers(UUID orgId) {
        return memberRepository.findByOrgId(orgId);
    }

    public List<OrganizationMember> getMembershipsByUserId(UUID userId) {
        return memberRepository.findByUserId(userId);
    }

    public Optional<MemberRole> findMemberRole(UUID orgId, UUID userId) {
        return memberRepository.findByOrgAndUser(orgId, userId).map(OrganizationMember::getMemberRole);
    }

    private long countAdmins(UUID orgId) {
        return memberRepository.findByOrgId(orgId).stream()
                .filter(member -> member.getMemberRole() == MemberRole.ADMIN)
                .count();
    }

    private String generateUniqueJoinCode() {
        for (int attempt = 0; attempt < MAX_JOIN_CODE_ATTEMPTS; attempt++) {
            String code = JoinCodeGenerator.generate();
            if (organizationRepository.findByJoinCode(code).isEmpty()) {
                return code;
            }
        }
        throw new ValidationException("Не удалось сгенерировать уникальный join-код, попробуйте ещё раз");
    }
}
