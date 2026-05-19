package com.hub.adapters.security.jwt;

import com.hub.application.auth.port.in.command.TokenGenerationCommand;
import com.hub.domain.identity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        jwtProvider = new JwtProvider();
        setField("privateKey", (RSAPrivateKey) keyPair.getPrivate());
        setField("publicKey", (RSAPublicKey) keyPair.getPublic());
    }

    @Test
    void generate_producesTokenWithExpectedClaims() {
        String tokenId = UUID.randomUUID().toString();
        TokenGenerationCommand command = new TokenGenerationCommand(
                42L, "alice", tokenId, Set.of(Role.ADMINISTRATOR),
                Instant.now().plusSeconds(3600));

        String token = jwtProvider.generate(command);

        Claims claims = jwtProvider.parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("alice");
        assertThat(claims.getId()).isEqualTo(tokenId);
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.get("roles")).asList().contains("ADMINISTRATOR");
    }

    @Test
    void isValid_returnsTrueForValidToken() {
        String token = generate("alice");
        assertThat(jwtProvider.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalseForExpiredToken() {
        TokenGenerationCommand command = new TokenGenerationCommand(
                1L, "alice", UUID.randomUUID().toString(),
                Set.of(Role.NON_ADMINISTRATOR), Instant.now().minusSeconds(1));

        String token = jwtProvider.generate(command);

        assertThat(jwtProvider.isValid(token)).isFalse();
    }

    @Test
    void isValid_returnsFalseForTamperedPayload() {
        String token = generate("alice");
        String[] parts = token.split("\\.");
        // Replace payload with a different base64 segment
        String tampered = parts[0] + ".dGFtcGVyZWQ." + parts[2];

        assertThat(jwtProvider.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_returnsFalseForTokenSignedWithDifferentKey() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair other = kpg.generateKeyPair();

        JwtProvider otherProvider = new JwtProvider();
        setFieldOn(otherProvider, "privateKey", (RSAPrivateKey) other.getPrivate());
        setFieldOn(otherProvider, "publicKey", (RSAPublicKey) other.getPublic());

        String tokenFromOther = otherProvider.generate(new TokenGenerationCommand(
                1L, "alice", UUID.randomUUID().toString(),
                Set.of(Role.NON_ADMINISTRATOR), Instant.now().plusSeconds(3600)));

        assertThat(jwtProvider.isValid(tokenFromOther)).isFalse();
    }

    @Test
    void isValid_returnsFalseForGarbageString() {
        assertThat(jwtProvider.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void parseClaims_throwsJwtExceptionForInvalidToken() {
        assertThatThrownBy(() -> jwtProvider.parseClaims("bad.token.value"))
                .isInstanceOf(JwtException.class);
    }

    private String generate(String username) {
        return jwtProvider.generate(new TokenGenerationCommand(
                1L, username, UUID.randomUUID().toString(),
                Set.of(Role.NON_ADMINISTRATOR), Instant.now().plusSeconds(3600)));
    }

    private void setField(String name, Object value) throws Exception {
        setFieldOn(jwtProvider, name, value);
    }

    private void setFieldOn(Object target, String name, Object value) throws Exception {
        Field f = JwtProvider.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
