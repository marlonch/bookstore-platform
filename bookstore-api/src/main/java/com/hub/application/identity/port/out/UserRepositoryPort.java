package com.hub.application.identity.port.out;

import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    void deleteById(UserId id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsById(UserId id);
}
