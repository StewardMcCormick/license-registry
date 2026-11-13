package repository;

import model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User create(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    List<User> findByOrganizationId(UUID orgId);

    List<User> findAll();

    void update(User user);

    void delete(UUID id);
}
