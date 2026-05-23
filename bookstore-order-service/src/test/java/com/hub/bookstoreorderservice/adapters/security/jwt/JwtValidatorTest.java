package com.hub.bookstoreorderservice.adapters.security.jwt;

import com.hub.bookstoreorderservice.support.TestJwtFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtValidatorTest {

    private JwtValidator validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new JwtValidator();
        ReflectionTestUtils.setField(validator, "publicKeyResource", new ClassPathResource("keys/public.pem"));
        ReflectionTestUtils.invokeMethod(validator, "init");
    }

    @Test
    void isValid_withValidToken_returnsTrue() {
        assertThat(validator.isValid(TestJwtFactory.token("alice", "ROLE_USER"))).isTrue();
    }

    @Test
    void isValid_withExpiredToken_returnsFalse() {
        assertThat(validator.isValid(TestJwtFactory.expiredToken("alice", "ROLE_USER"))).isFalse();
    }

    @Test
    void isValid_withTamperedPayload_returnsFalse() {
        String token = TestJwtFactory.token("alice", "ROLE_USER");
        String[] parts = token.split("\\.");
        String tampered = parts[0] + ".dGFtcGVyZWQ" + "." + parts[2];
        assertThat(validator.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_withDifferentKeySignature_returnsFalse() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("alice")
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .claim("roles", List.of("ROLE_USER"))
                .signWith(pair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThat(validator.isValid(token)).isFalse();
    }

    @Test
    void isValid_withNullToken_returnsFalse() {
        assertThat(validator.isValid(null)).isFalse();
    }

    @Test
    void isValid_withEmptyToken_returnsFalse() {
        assertThat(validator.isValid("")).isFalse();
    }

    @Test
    void isValid_withMalformedToken_returnsFalse() {
        assertThat(validator.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void parseClaims_returnsCorrectSubject() {
        Claims claims = validator.parseClaims(TestJwtFactory.token("bob", "ROLE_USER"));
        assertThat(claims.getSubject()).isEqualTo("bob");
    }

    @Test
    void parseClaims_returnsCorrectRoles() {
        Claims claims = validator.parseClaims(TestJwtFactory.token("alice", "ROLE_USER", "ADMINISTRATOR"));
        assertThat(claims.get("roles", List.class))
                .containsExactlyInAnyOrder("ROLE_USER", "ADMINISTRATOR");
    }

    @Test
    void parseClaims_returnsNonEmptyJti() {
        Claims claims = validator.parseClaims(TestJwtFactory.token("alice", "ROLE_USER"));
        assertThat(claims.getId()).isNotBlank();
    }

    @Test
    void init_withInvalidPublicKeyResource_throwsIllegalStateException() {
        JwtValidator v = new JwtValidator();
        ReflectionTestUtils.setField(v, "publicKeyResource", new ByteArrayResource("not!!valid!!base64".getBytes()));

        assertThatThrownBy(v::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to load RSA public key");
    }

    @Test
    void parseClaims_withInvalidToken_throwsJwtException() {
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> validator.parseClaims("invalid.token.value"));
    }
}
