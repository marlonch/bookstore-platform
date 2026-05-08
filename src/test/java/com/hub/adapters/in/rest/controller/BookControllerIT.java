package com.hub.adapters.in.rest.controller;

import com.hub.adapters.in.rest.dto.request.CreateBookRequest;
import com.hub.adapters.in.rest.dto.request.LoginRequest;
import com.hub.adapters.in.rest.dto.response.BookResponse;
import com.hub.adapters.in.rest.dto.response.LoginResponse;
import com.hub.adapters.out.persistence.jpa.UserJpaAdapter;
import com.hub.application.auth.port.out.PasswordHasherPort;
import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.domain.auth.TokenMetadata;
import com.hub.domain.auth.TokenStatus;
import com.hub.domain.identity.Role;
import com.hub.domain.identity.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// H2 state isolation: each test starts with a clean schema because ddl-auto=create-drop runs per context
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookControllerIT {

    private static final String RAW_PASSWORD = "Password123!";
    private static final String ADMIN_USERNAME = "adminuser";
    private static final String USER_USERNAME = "testuser";

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @Autowired
    private UserJpaAdapter userJpaAdapter;

    @Autowired
    private PasswordHasherPort passwordHasherPort;

    @MockitoBean
    private TokenMetadataRepositoryPort tokenMetadataRepositoryPort;

    private String userToken;
    private String adminToken;

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
                                .userId(1L)
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(3600))
                                .status(TokenStatus.ACTIVE)
                                .build()));

        userJpaAdapter.save(User.builder()
                .username(USER_USERNAME)
                .email("user@hub.com")
                .passwordHash(passwordHasherPort.encode(RAW_PASSWORD))
                .roles(Set.of(Role.NON_ADMINISTRATOR))
                .status(UserStatus.ACTIVE)
                .build());

        userJpaAdapter.save(User.builder()
                .username(ADMIN_USERNAME)
                .email("admin@hub.com")
                .passwordHash(passwordHasherPort.encode(RAW_PASSWORD))
                .roles(Set.of(Role.ADMINISTRATOR))
                .status(UserStatus.ACTIVE)
                .build());

        userToken = login(USER_USERNAME);
        adminToken = login(ADMIN_USERNAME);
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

    // --- list books ---

    @Test
    void listBooks_asAuthenticatedUser_returns200() {
        webTestClient.get()
                .uri("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listBooks_withoutToken_returns401() {
        webTestClient.get()
                .uri("/api/books")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // --- get by id ---

    @Test
    void getBook_byId_asAuthenticatedUser_returns200() {
        BookResponse created = createBookAsAdmin("Clean Code", "Robert C. Martin", 2008);

        webTestClient.get()
                .uri("/api/books/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookResponse.class)
                .value(book -> {
                    assertThat(book.id()).isEqualTo(created.id());
                    assertThat(book.title()).isEqualTo("Clean Code");
                });
    }

    @Test
    void getBook_byId_whenNotFound_returns404() {
        webTestClient.get()
                .uri("/api/books/{id}", 9999L)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    // --- create book ---

    @Test
    void createBook_asAdmin_returns201WithBody() {
        webTestClient.post()
                .uri("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateBookRequest("Domain-Driven Design", "Eric Evans", 2003))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookResponse.class)
                .value(book -> {
                    assertThat(book.id()).isNotNull();
                    assertThat(book.title()).isEqualTo("Domain-Driven Design");
                    assertThat(book.author()).isEqualTo("Eric Evans");
                });
    }

    @Test
    void createBook_asNonAdmin_returns403() {
        webTestClient.post()
                .uri("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateBookRequest("Title", "Author", 2020))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createBook_withBlankTitle_returns400() {
        webTestClient.post()
                .uri("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateBookRequest("", "Some Author", 2020))
                .exchange()
                .expectStatus().isBadRequest();
    }

    // --- auth ---

    @Test
    void login_withWrongPassword_returns401() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(USER_USERNAME, "wrongpassword"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void logout_asAuthenticated_returns204_thenSubsequentRequest_returns401() {
        // Confirm the token works before logout
        webTestClient.get()
                .uri("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk();

        // Logout
        webTestClient.post()
                .uri("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isNoContent();

        // Simulate Redis now returning REVOKED for the same token id
        when(tokenMetadataRepositoryPort.findByTokenId(anyString()))
                .thenReturn(Optional.of(
                        TokenMetadata.builder()
                                .tokenId("test-token-id")
                                .userId(1L)
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(3600))
                                .status(TokenStatus.REVOKED)
                                .build()));

        webTestClient.get()
                .uri("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // --- helpers ---

    private BookResponse createBookAsAdmin(String title, String author, int year) {
        return webTestClient.post()
                .uri("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateBookRequest(title, author, year))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
