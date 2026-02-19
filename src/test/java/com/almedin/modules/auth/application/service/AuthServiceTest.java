package com.almedin.modules.auth.application.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.almedin.modules.admin.domain.model.Admin;
import com.almedin.modules.admin.domain.repository.AdminRepository;
import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import com.almedin.modules.auth.application.dto.AuthRequest;
import com.almedin.modules.auth.application.dto.AuthResponse;
import com.almedin.modules.shared.domain.enums.Role;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class AuthServiceTest {

    @InjectMock
    AffiliateRepository affiliateRepository;

    @InjectMock
    SpecialistRepository specialistRepository;

    @InjectMock
    AdminRepository adminRepository;

    @Inject
    AuthService authService;

    private Affiliate affiliate;
    private Specialist specialist;
    private Admin admin;

    private static final String RAW_PASSWORD = "password123";
    private static final String HASHED_PASSWORD =
            BCrypt.withDefaults().hashToString(12, RAW_PASSWORD.toCharArray());

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setId(1L);
        affiliate.setFirstName("Juan");
        affiliate.setLastName("Pérez");
        affiliate.setDni("12345678");
        affiliate.setEmail("juan@email.com");
        affiliate.setRole(Role.AFFILIATE);
        affiliate.setPassword(HASHED_PASSWORD);
        affiliate.setActive(true);

        specialist = new Specialist();
        specialist.setId(2L);
        specialist.setFirstName("Laura");
        specialist.setLastName("Gómez");
        specialist.setDni("22334455");
        specialist.setEmail("laura@email.com");
        specialist.setRole(Role.SPECIALIST);
        specialist.setPassword(HASHED_PASSWORD);
        specialist.setActive(true);

        admin = new Admin();
        admin.setId(3L);
        admin.setFirstName("Super");
        admin.setLastName("Admin");
        admin.setDni("00000000");
        admin.setEmail("admin@almedin.com");
        admin.setRole(Role.ADMIN);
        admin.setPassword(HASHED_PASSWORD);
        admin.setActive(true);
    }

    @Test
    void login_afiliadoConCredencialesValidas_debeRetornarTokenConRolAfiliado() {
        when(affiliateRepository.findByEmail("juan@email.com")).thenReturn(Optional.of(affiliate));

        AuthResponse response = authService.login(new AuthRequest("juan@email.com", RAW_PASSWORD));

        assertThat(response).isNotNull();
        assertThat(response.role()).isEqualTo("AFFILIATE");
        assertThat(response.fullName()).isEqualTo("Juan Pérez");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.token()).isNotBlank();
    }


    @Test
    void login_especialistaConCredencialesValidas_debeRetornarTokenConRolEspecialista() {
        when(affiliateRepository.findByEmail("laura@email.com")).thenReturn(Optional.empty());
        when(specialistRepository.findByEmail("laura@email.com")).thenReturn(Optional.of(specialist));

        AuthResponse response = authService.login(new AuthRequest("laura@email.com", RAW_PASSWORD));

        assertThat(response.role()).isEqualTo("SPECIALIST");
        assertThat(response.fullName()).isEqualTo("Laura Gómez");
        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.token()).isNotBlank();
    }


    @Test
    void login_adminConCredencialesValidas_debeRetornarTokenConRolAdmin() {
        when(affiliateRepository.findByEmail("admin@almedin.com")).thenReturn(Optional.empty());
        when(specialistRepository.findByEmail("admin@almedin.com")).thenReturn(Optional.empty());
        when(adminRepository.findByEmail("admin@almedin.com")).thenReturn(Optional.of(admin));

        AuthResponse response = authService.login(new AuthRequest("admin@almedin.com", RAW_PASSWORD));

        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.token()).isNotBlank();
    }


    @Test
    void login_emailInexistente_debeLanzarException() {
        when(affiliateRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(specialistRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(adminRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new AuthRequest("noexiste@email.com", RAW_PASSWORD)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void login_passwordIncorrecto_debeLanzarException() {
        when(affiliateRepository.findByEmail("juan@email.com")).thenReturn(Optional.of(affiliate));

        assertThatThrownBy(() -> authService.login(
                new AuthRequest("juan@email.com", "passwordMal")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales inválidas");
    }


    @Test
    void login_cuentaDesactivada_debeLanzarException() {
        affiliate.setActive(false);
        when(affiliateRepository.findByEmail("juan@email.com")).thenReturn(Optional.of(affiliate));

        assertThatThrownBy(() -> authService.login(
                new AuthRequest("juan@email.com", RAW_PASSWORD)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("desactivada");
    }
}