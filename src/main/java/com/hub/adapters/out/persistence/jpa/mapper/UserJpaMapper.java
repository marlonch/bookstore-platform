package com.hub.adapters.out.persistence.jpa.mapper;

import com.hub.adapters.out.persistence.jpa.entity.UserJpaEntity;
import com.hub.domain.identity.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Maps domain {@link User} aggregates to and from their JPA persistence
 * representation.
 *
 * <p>This mapper isolates persistence conversion logic from the domain model,
 * preventing persistence concerns from leaking into the core business layer.</p>
 */
@Component
public class UserJpaMapper {

    /**
     * Maps a domain {@link User} aggregate into its JPA persistence
     * representation.
     *
     * <p>A defensive copy of the role collection is created to avoid sharing
     * mutable state between the domain and persistence layers.</p>
     *
     * @param user the domain aggregate to convert
     * @return the persistence entity created from the domain aggregate
     */
    public UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .roles(new HashSet<>(user.getRoles()))
                .status(user.getStatus())
                .build();
    }

    /**
     * Maps a JPA {@link UserJpaEntity} into the domain {@link User} aggregate.
     *
     * <p>A defensive copy of the role collection is created to preserve
     * encapsulation between persistence and domain representations.</p>
     *
     * @param entity the persistence entity to convert
     * @return the domain aggregate created from the persistence entity
     */
    public User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .roles(new HashSet<>(entity.getRoles()))
                .status(entity.getStatus())
                .build();
    }
}

