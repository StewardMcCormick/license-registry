package repository;

import model.MemberRole;
import model.OrganizationMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository {

    OrganizationMember create(OrganizationMember member);

    Optional<OrganizationMember> findByOrgAndUser(UUID orgId, UUID userId);

    List<OrganizationMember> findByOrgId(UUID orgId);

    List<OrganizationMember> findByUserId(UUID userId);

    void updateRole(UUID orgId, UUID userId, MemberRole memberRole);

    void delete(UUID orgId, UUID userId);
}
