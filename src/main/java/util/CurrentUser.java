package util;

import model.GlobalRole;
import model.MemberRole;

import java.io.Serializable;
import java.util.UUID;

public class CurrentUser implements Serializable {

    private final UUID userId;
    private final String email;
    private final String fullName;
    private final GlobalRole globalRole;
    private UUID orgId;
    private MemberRole memberRole;

    public CurrentUser(UUID userId, String email, String fullName, GlobalRole globalRole) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.globalRole = globalRole;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public GlobalRole getGlobalRole() {
        return globalRole;
    }

    public boolean isSuperadmin() {
        return globalRole == GlobalRole.SUPERADMIN;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    public MemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }
}
