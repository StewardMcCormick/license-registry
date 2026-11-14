package service;

import exception.NotFoundException;
import exception.ValidationException;
import model.Tag;
import repository.TagRepository;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public class TagService {

    private static final int MAX_NAME_LENGTH = 50;

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag getOrCreate(Connection connection, UUID orgId, String name) {
        String normalized = normalize(name);
        return tagRepository.findByOrgIdAndName(connection, orgId, normalized)
                .orElseGet(() -> tagRepository.create(connection, new Tag(orgId, normalized)));
    }

    public Tag getById(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Тег не найден"));
    }

    public List<Tag> getByOrgId(UUID orgId) {
        return tagRepository.findByOrgId(orgId);
    }

    public void rename(UUID id, String newName) {
        Tag tag = getById(id);
        String normalized = normalize(newName);
        tagRepository.findByOrgIdAndName(tag.getOrgId(), normalized).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ValidationException("Такой тег уже существует в организации");
            }
        });
        tag.setName(normalized);
        tagRepository.update(tag);
    }

    public void delete(UUID id) {
        getById(id);
        tagRepository.delete(id);
    }

    private String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название тега обязательно");
        }
        String normalized = name.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Название тега не должно превышать " + MAX_NAME_LENGTH + " символов");
        }
        return normalized;
    }
}
