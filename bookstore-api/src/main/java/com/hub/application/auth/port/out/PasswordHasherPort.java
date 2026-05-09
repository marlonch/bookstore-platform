package com.hub.application.auth.port.out;

/**
 * Outbound port for generating and verifying password hashes.
 * <p>
 * This contract isolates the application core from the concrete password
 * hashing implementation used by the infrastructure layer.
 */
public interface PasswordHasherPort {

    /**
     * Generates a secure hash for the given raw password.
     *
     * @param rawPassword plain-text password to hash
     * @return hashed password suitable for persistence
     */
    String encode(String rawPassword);

    /**
     * Verifies whether the given raw password matches the stored hash.
     *
     * @param rawPassword plain-text password provided by the user
     * @param hashedPassword stored password hash
     * @return {@code true} if the password matches the stored hash,
     * otherwise {@code false}
     */
    boolean matches(String rawPassword, String hashedPassword);
}
