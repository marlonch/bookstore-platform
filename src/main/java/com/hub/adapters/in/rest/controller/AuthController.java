package com.hub.adapters.in.rest.controller;

import com.hub.adapters.in.rest.dto.request.LoginRequest;
import com.hub.adapters.in.rest.dto.response.LoginResponse;
import com.hub.application.auth.port.in.LoginUseCase;
import com.hub.application.auth.port.in.LogoutUseCase;
import com.hub.application.auth.port.in.command.LoginCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.login(new LoginCommand(request.username(), request.password()));
        return ResponseEntity.ok(new LoginResponse(result.token()));
    }

    /** Logout invalidates the current JWT by marking it REVOKED in Redis. */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(Principal principal) {
        String tokenId = (String) ((UsernamePasswordAuthenticationToken) principal).getCredentials();
        logoutUseCase.logout(tokenId);
        return ResponseEntity.noContent().build();
    }
}
