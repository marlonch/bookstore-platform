package com.hub.adapters.out.persistence.jpa.mapper;

import com.hub.adapters.out.persistence.jpa.entity.UserJpaEntity;
import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class UserJpaMapper {

    public UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId().value())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .roles(new HashSet<>(user.getRoles()))
                .status(user.getStatus())
                .build();
    }

    public User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(new UserId(entity.getId()))
                .username(entity.getUsername())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .roles(new HashSet<>(entity.getRoles()))
                .status(entity.getStatus())
                .build();
    }
}
