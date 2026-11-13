package service;

import exception.NotFoundException;
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
        Organization organization = new Organization(name.trim(), generateUniqueJoinCode());
        organization = organizationRepository.create(organization);
        memberRepository.create(new OrganizationMember(organization.getId(), creatorUserId, MemberRole.ADMIN));
        return organization;
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
        memberRepository.create(new OrganizationMember(organization.getId(), userId, MemberRole.MANAGER));
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
