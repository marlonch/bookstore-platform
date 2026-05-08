package com.hub.application.identity.port.out;


import com.hub.domain.identity.User;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for persisting and retrieving {@link User} domain objects.
 * <p>
 * This contract abstracts the persistence operations required by the
 * application layer to manage users.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    void deleteById(Long id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsById(Long id);
}
