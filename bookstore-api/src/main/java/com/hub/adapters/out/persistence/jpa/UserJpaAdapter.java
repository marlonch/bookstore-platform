package com.hub.adapters.out.persistence.jpa;

import com.hub.adapters.out.persistence.jpa.mapper.UserJpaMapper;
import com.hub.adapters.out.persistence.jpa.repository.UserJpaRepository;
import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;
    private final UserJpaMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UserId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsById(UserId id) {
        return jpaRepository.existsById(id.value());
    }
}
