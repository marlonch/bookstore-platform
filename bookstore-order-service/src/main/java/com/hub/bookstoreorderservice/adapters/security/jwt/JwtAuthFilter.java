package com.hub.bookstoreorderservice.adapters.security.jwt;

import com.hub.bookstoreorderservice.adapters.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Stateless JWT authentication filter for order-service.
 * <p>
 * Unlike bookstore-api's filter, this implementation does not check Redis for token
 * revocation — it performs signature-only validation using the RSA public key.
 * The raw token string is stored as {@code credentials} in the authentication object
 * so that {@link com.hub.bookstoreorderservice.adapters.out.catalog.feign.JwtPropagationInterceptor}
 * can forward it to bookstore-api without re-parsing.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtValidator.isValid(token)) {
            chain.doFilter(request, response);
            return;
        }

        Claims claims = jwtValidator.parseClaims(token);
        String username = claims.getSubject();

        List<SimpleGrantedAuthority> authorities = extractRoles(claims);
        UserDetailsImpl userDetails = new UserDetailsImpl(username, authorities);

        // Raw token stored as credentials so JwtPropagationInterceptor can forward it.
        var auth = new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (!(rolesObj instanceof List<?> rolesList)) {
            return List.of();
        }
        return rolesList.stream()
                .filter(String.class::isInstance)
                .map(r -> new SimpleGrantedAuthority((String) r))
                .toList();
    }
}
