package com.hub.adapters.security.jwt;

import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenMetadataRepositoryPort tokenMetadataPort;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtProvider.isValid(token)) {
            chain.doFilter(request, response);
            return;
        }

        var claims = jwtProvider.parseClaims(token);
        String tokenId = claims.getId();
        String username = claims.getSubject();

        var metadataOpt = tokenMetadataPort.findByTokenId(tokenId);
        if (metadataOpt.isEmpty() || !metadataOpt.get().isActive()) {
            chain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // tokenId stored as credential so AuthController.logout() can extract it
        var authToken = new UsernamePasswordAuthenticationToken(
                userDetails, tokenId, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        chain.doFilter(request, response);
    }
}
