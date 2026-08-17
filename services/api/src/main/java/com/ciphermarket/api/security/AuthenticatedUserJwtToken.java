package com.ciphermarket.api.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public class AuthenticatedUserJwtToken extends JwtAuthenticationToken {

    private final AuthenticatedUser user;

    public AuthenticatedUserJwtToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, AuthenticatedUser user) {
        super(jwt, authorities, user.keycloakSub());
        this.user = user;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
}
