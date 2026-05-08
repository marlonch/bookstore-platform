package com.hub.application.auth.port.out;

import com.hub.application.auth.port.in.command.TokenGenerationCommand;

/**
 * Outbound port for generating authentication tokens. eg JWT
 * <p>
 * This contract isolates the application core from the concrete token
 * generation mechanism used by the infrastructure layer.
 */
public interface TokenGeneratorPort {

    /**
     * Generates an authentication token from the provided token claims and metadata.
     *
     * @param command token generation input data
     * @return generated authentication token
     */
    String generate(TokenGenerationCommand command);
}
