package com.hub.bookstoreorderservice.adapters.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Spring Security {@link UserDetails} implementation built directly from JWT claims.
 * order-service does not maintain a users database; identity is derived entirely
 * from the validated token.
 */
public class UserDetailsImpl implements UserDetails {

    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(String username, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.authorities = authorities;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return username; }
}
