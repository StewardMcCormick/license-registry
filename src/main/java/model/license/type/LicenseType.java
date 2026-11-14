package model.license.type;

import java.util.UUID;

public class LicenseType {

    private UUID id;
    private UUID orgId;
    private String code;
    private String name;

    public LicenseType() {
    }

    public LicenseType(UUID orgId, String code, String name) {
        this.orgId = orgId;
        this.code = code;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return code + "-" + name;
    }
}
