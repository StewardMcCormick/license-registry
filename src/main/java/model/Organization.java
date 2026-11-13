package model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Organization {

    private UUID id;
    private String name;
    private String joinCode;
    private OffsetDateTime createdAt;

    public Organization() {
    }

    public Organization(String name, String joinCode) {
        this.name = name;
        this.joinCode = joinCode;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
