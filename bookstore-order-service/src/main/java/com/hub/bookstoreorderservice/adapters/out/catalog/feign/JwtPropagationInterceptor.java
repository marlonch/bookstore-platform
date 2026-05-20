package com.hub.bookstoreorderservice.adapters.out.catalog.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Feign {@link RequestInterceptor} that forwards the authenticated user's Bearer
 * token to downstream services, enabling token propagation across service boundaries
 * without re-authentication.
 * <p>
 * The raw token string is stored as {@code credentials} in the
 * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
 * by {@link com.hub.bookstoreorderservice.adapters.security.jwt.JwtAuthFilter}.
 */
@Component
public class JwtPropagationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String token) {
            template.header("Authorization", "Bearer " + token);
        }
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            template.header("X-Correlation-Id", correlationId);
        }
    }
}
