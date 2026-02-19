package com.almedin.modules.shared.application.security;

import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
public class SecurityContext {

    @Inject
    JsonWebToken jwt;

    public Long getCurrentUserId() {
        Object userId = jwt.getClaim("userId");
        if (userId == null) throw new UnauthorizedException("Token inválido");
        if (userId instanceof Number) return ((Number) userId).longValue();
        try {
            return Long.parseLong(userId.toString());
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Token inválido");
        }
    }

    public String getCurrentRole() {
        return jwt.getGroups().stream()
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Token inválido"));
    }

    public boolean isAdmin() {
        return jwt.getGroups().contains("ADMIN");
    }

    public void requireSelfOrAdmin(Long resourceOwnerId) {
        if (isAdmin()) return;
        if (!getCurrentUserId().equals(resourceOwnerId)) {
            throw new UnauthorizedException("No tenés permiso para acceder a este recurso");
        }
    }
}