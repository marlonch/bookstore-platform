package com.hub.adapters.out.session.redis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/** Enables Spring Data Redis repositories with keyspace event support for @Indexed queries. */
@Configuration
@Profile("!test")
@EnableRedisRepositories(
        basePackages = "com.hub.adapters.out.session.redis",
        enableKeyspaceEvents = org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP
)
public class RedisConfig {
}
