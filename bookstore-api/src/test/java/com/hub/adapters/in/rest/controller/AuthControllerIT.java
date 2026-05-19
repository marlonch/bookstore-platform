package com.hub.adapters.in.rest.controller;

import com.hub.adapters.in.rest.dto.request.LoginRequest;
import com.hub.adapters.in.rest.dto.response.LoginResponse;
import com.hub.adapters.out.persistence.jpa.UserJpaAdapter;
import com.hub.application.auth.port.out.PasswordHasherPort;
import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.domain.auth.TokenMetadata;
import com.hub.domain.auth.TokenStatus;
import com.hub.domain.identity.Role;
import com.hub.domain.identity.User;
import com.hub.domain.identity.UserId;
import com.hub.domain.identity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerIT {

    private static final String RAW_PASSWORD = "Password123!";
    private static final String USERNAME = "testuser";

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @Autowired
    private UserJpaAdapter userJpaAdapter;

    @Autowired
    private PasswordHasherPort passwordHasherPort;

    @MockitoBean
    private TokenMetadataRepositoryPort tokenMetadataRepositoryPort;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        Mockito.reset(tokenMetadataRepositoryPort);
        doNothing().when(tokenMetadataRepositoryPort).save(any());
        doNothing().when(tokenMetadataRepositoryPort).revokeToken(anyString());

        when(tokenMetadataRepositoryPort.findByTokenId(anyString()))
                .thenReturn(Optional.of(
                        TokenMetadata.builder()
                                .tokenId("test-token-id")
                                .userId(UUID.randomUUID())
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(3600))
                                .status(TokenStatus.ACTIVE)
                                .build()));

        userJpaAdapter.save(User.builder()
                .id(UserId.generate())
                .username(USERNAME)
                .email("user@hub.com")
                .passwordHash(passwordHasherPort.encode(RAW_PASSWORD))
                .roles(Set.of(Role.NON_ADMINISTRATOR))
                .status(UserStatus.ACTIVE)
                .build());
    }

    @Test
    void login_withValidCredentials_returns200WithToken() {
        LoginResponse response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(USERNAME, RAW_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void login_withWrongPassword_returns401() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(USERNAME, "wrongpassword"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_withUnknownUsername_returns401() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("ghost", RAW_PASSWORD))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_withBlankUsername_returns400() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("", RAW_PASSWORD))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void login_withBlankPassword_returns400() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(USERNAME, ""))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void logout_withValidToken_returns204() {
        String token = login(USERNAME);

        webTestClient.post()
                .uri("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void logout_withoutToken_returns401() {
        webTestClient.post()
                .uri("/api/auth/logout")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private String login(String username) {
        LoginResponse response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(username, RAW_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).as("Login must succeed for user: " + username).isNotNull();
        return response.token();
    }
}
