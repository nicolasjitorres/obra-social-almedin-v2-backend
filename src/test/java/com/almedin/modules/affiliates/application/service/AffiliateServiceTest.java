package com.almedin.modules.affiliates.application.service;

import com.almedin.modules.affiliates.application.dto.AffiliateRequest;
import com.almedin.modules.affiliates.application.dto.AffiliateResponse;
import com.almedin.modules.affiliates.domain.exceptions.AffiliateNotFoundException;
import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class AffiliateServiceTest {

    @InjectMock
    AffiliateRepository affiliateRepository;

    @Inject
    AffiliateService affiliateService;

    private Affiliate affiliate;
    private AffiliateRequest request;

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setId(1L);
        affiliate.setFirstName("Juan");
        affiliate.setLastName("Pérez");
        affiliate.setDni("12345678");
        affiliate.setEmail("juan@email.com");
        affiliate.setHealthInsuranceCode("OS-001");

        request = new AffiliateRequest(
                "Juan", "Pérez", "12345678", "juan@email.com", "OS-001"
        );
    }

    @Test
    void findAll_debeRetornarListaDeAfiliados() {
        when(affiliateRepository.listAll()).thenReturn(List.of(affiliate));

        List<AffiliateResponse> result = affiliateService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).dni()).isEqualTo("12345678");
        verify(affiliateRepository, times(1)).listAll();
    }

    @Test
    void findById_cuandoExiste_debeRetornarAfiliado() {
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(affiliate));

        AffiliateResponse result = affiliateService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("juan@email.com");
    }

    @Test
    void findById_cuandoNoExiste_debeLanzarNotFoundException() {
        when(affiliateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.findById(99L))
                .isInstanceOf(AffiliateNotFoundException.class);
    }

    @Test
    void create_cuandoDniDuplicado_debeLanzarIllegalArgumentException() {
        when(affiliateRepository.findByDni("12345678"))
                .thenReturn(Optional.of(affiliate));

        assertThatThrownBy(() -> affiliateService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DNI");
    }

    @Test
    void create_cuandoCodigoObraSocialDuplicado_debeLanzarIllegalArgumentException() {
        when(affiliateRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(affiliateRepository.findByHealthInsuranceCode("OS-001"))
                .thenReturn(Optional.of(affiliate));

        assertThatThrownBy(() -> affiliateService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obra social");
    }

    @Test
    void create_cuandoDatosValidos_debeRetornarAfiliadoCreado() {
        when(affiliateRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(affiliateRepository.findByHealthInsuranceCode("OS-001")).thenReturn(Optional.empty());
        doNothing().when(affiliateRepository).persist(any(Affiliate.class));

        AffiliateResponse result = affiliateService.create(request);

        assertThat(result.dni()).isEqualTo("12345678");
        assertThat(result.email()).isEqualTo("juan@email.com");
        verify(affiliateRepository).persist(any(Affiliate.class));
    }

    @Test
    void update_cuandoDniCambiado_yYaExiste_debeLanzarConflicto() {
        AffiliateRequest requestConDniNuevo = new AffiliateRequest(
                "Juan", "Pérez", "99999999", "juan@email.com", "OS-001"
        );
        Affiliate otraPersona = new Affiliate();
        otraPersona.setDni("99999999");

        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(affiliate));
        when(affiliateRepository.findByDni("99999999"))
                .thenReturn(Optional.of(otraPersona));

        assertThatThrownBy(() -> affiliateService.update(1L, requestConDniNuevo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DNI");
    }

    @Test
    void deactivate_cuandoNoExiste_debeLanzarNotFoundException() {
        when(affiliateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.deactivate(99L))
                .isInstanceOf(AffiliateNotFoundException.class);

        verify(affiliateRepository, never()).deactivate(any());
    }
}