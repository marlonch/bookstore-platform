package com.hub.adapters.out.session.redis.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("token_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenMetadataRedisEntity {

    @Id
    private String tokenId;

    @Indexed
    private Long userId;

    private long issuedAtEpoch;
    private long expiresAtEpoch;
    private String status;

    @TimeToLive
    private Long timeToLiveSeconds;
}
