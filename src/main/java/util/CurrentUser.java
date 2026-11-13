package util;

import model.GlobalRole;
import model.MemberRole;

import java.io.Serializable;
import java.util.*;

public class CurrentUser implements Serializable {

    private final UUID userId;
    private final String email;
    private final String fullName;
    private final GlobalRole globalRole;
    private final Map<UUID, MemberRole> memberRoleMap = new HashMap<>();
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

    public MemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public Map<UUID, MemberRole> getMemberRoleMap() {
        return memberRoleMap;
    }

    public void addMemberRoleMap(UUID orgId, MemberRole role) {
        memberRoleMap.put(orgId, role);
    }
}
