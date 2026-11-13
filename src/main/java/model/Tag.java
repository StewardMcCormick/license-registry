package model;

import java.util.UUID;

public class Tag {

    private UUID id;
    private UUID orgId;
    private String name;

    public Tag() {
    }

    public Tag(UUID orgId, String name) {
        this.orgId = orgId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
