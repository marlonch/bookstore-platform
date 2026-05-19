package com.hub.adapters.out.session.redis.repository;

import com.hub.adapters.out.session.redis.entity.TokenMetadataRedisEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TokenMetadataRedisRepository extends CrudRepository<TokenMetadataRedisEntity, String> {

    List<TokenMetadataRedisEntity> findByUserId(UUID userId);
}
