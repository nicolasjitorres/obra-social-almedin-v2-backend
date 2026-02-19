package com.almedin.modules.shared.application.security;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
class SecurityContextTest {

    @Inject
    SecurityContext securityContext;

    @Test
    @TestSecurity(user = "admin@almedin.com", roles = "ADMIN")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "1")})
    void getCurrentUserId_conTokenValido_debeRetornarId() {
        assertThat(securityContext.getCurrentUserId()).isEqualTo(1L);
    }

    @Test
    @TestSecurity(user = "admin@almedin.com", roles = "ADMIN")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "1")})
    void getCurrentRole_conTokenValido_debeRetornarRol() {
        assertThat(securityContext.getCurrentRole()).isEqualTo("ADMIN");
    }

    @Test
    @TestSecurity(user = "admin@almedin.com", roles = "ADMIN")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "1")})
    void isAdmin_cuandoEsAdmin_debeRetornarTrue() {
        assertThat(securityContext.isAdmin()).isTrue();
    }

    @Test
    @TestSecurity(user = "afiliado@almedin.com", roles = "AFFILIATE")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "2")})
    void isAdmin_cuandoNoEsAdmin_debeRetornarFalse() {
        assertThat(securityContext.isAdmin()).isFalse();
    }

    @Test
    @TestSecurity(user = "afiliado@almedin.com", roles = "AFFILIATE")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "2")})
    void requireSelfOrAdmin_cuandoEsElMismoUsuario_noDebeLanzarExcepcion() {
        securityContext.requireSelfOrAdmin(2L);
    }

    @Test
    @TestSecurity(user = "afiliado@almedin.com", roles = "AFFILIATE")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "2")})
    void requireSelfOrAdmin_cuandoEsOtroUsuario_debeLanzarUnauthorized() {
        assertThatThrownBy(() -> securityContext.requireSelfOrAdmin(99L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @TestSecurity(user = "admin@almedin.com", roles = "ADMIN")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "1")})
    void requireSelfOrAdmin_cuandoEsAdmin_puedeAccederARecursoAjeno() {
        securityContext.requireSelfOrAdmin(999L);
    }
}