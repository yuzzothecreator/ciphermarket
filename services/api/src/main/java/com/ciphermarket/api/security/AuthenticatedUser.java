package com.ciphermarket.api.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record AuthenticatedUser(
        String keycloakSub,
        String email,
        String displayName,
        Set<String> roles
) implements org.springframework.security.core.userdetails.UserDetails {

    public static AuthenticatedUser fromJwt(Jwt jwt) {
        String sub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("preferred_username");
        if (name == null) {
            name = jwt.getClaimAsString("name");
        }

        Set<String> roles = extractRealmRoles(jwt);
        return new AuthenticatedUser(sub, email, name, roles);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Set.of("buyer");
        }
        Object rolesObj = realmAccess.get("roles");
        if (rolesObj instanceof List<?> roles) {
            return roles.stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of("buyer");
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return keycloakSub;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
