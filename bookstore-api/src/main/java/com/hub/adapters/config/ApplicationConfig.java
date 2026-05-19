package com.hub.adapters.config;

import com.hub.application.auth.port.out.PasswordHasherPort;
import com.hub.application.auth.port.out.TokenGeneratorPort;
import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.application.auth.service.AuthService;
import com.hub.application.identity.port.out.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Value("${app.jwt.expiration-hours:24}")
    private long jwtExpirationHours;

    @Bean
    public AuthService authService(
            UserRepositoryPort userRepository,
            TokenMetadataRepositoryPort tokenMetadataPort,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokenGenerator) {
        return new AuthService(userRepository, tokenMetadataPort, passwordHasher, tokenGenerator, jwtExpirationHours);
    }
}