package model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class OrganizationMember {

    private UUID orgId;
    private UUID userId;
    private MemberRole memberRole;
    private OffsetDateTime joinedAt;

    public OrganizationMember() {
    }

    public OrganizationMember(UUID orgId, UUID userId, MemberRole memberRole) {
        this.orgId = orgId;
        this.userId = userId;
        this.memberRole = memberRole;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public MemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(OffsetDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
