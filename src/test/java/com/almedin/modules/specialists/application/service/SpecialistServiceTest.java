package com.almedin.modules.specialists.application.service;

import com.almedin.modules.specialists.application.dto.SpecialistRequest;
import com.almedin.modules.specialists.application.dto.SpecialistResponse;
import com.almedin.modules.specialists.domain.exceptions.SpecialistNotFoundException;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import com.almedin.modules.shared.domain.enums.Speciality;
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
class SpecialistServiceTest {

    @InjectMock
    SpecialistRepository specialistRepository;

    @Inject
    SpecialistService specialistService;

    private Specialist specialist;
    private SpecialistRequest request;

    @BeforeEach
    void setUp() {
        specialist = new Specialist();
        specialist.setId(1L);
        specialist.setFirstName("Laura");
        specialist.setLastName("Gómez");
        specialist.setDni("22334455");
        specialist.setEmail("laura@email.com");
        specialist.setSpeciality(Speciality.CARDIOLOGIA);
        specialist.setAddress("Av. Corrientes 1234");

        request = new SpecialistRequest(
                "Laura", "Gómez", "22334455", "laura@email.com",
                Speciality.CARDIOLOGIA, "Av. Corrientes 1234"
        );
    }

    @Test
    void findAll_debeRetornarListaDeEspecialistas() {
        when(specialistRepository.listAllSpecialists()).thenReturn(List.of(specialist));

        List<SpecialistResponse> result = specialistService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).dni()).isEqualTo("22334455");
        verify(specialistRepository, times(1)).listAllSpecialists();
    }

    @Test
    void findById_cuandoExiste_debeRetornarEspecialista() {
        when(specialistRepository.findSpecialistById(1L)).thenReturn(Optional.of(specialist));

        SpecialistResponse result = specialistService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("laura@email.com");
    }

    @Test
    void findById_cuandoNoExiste_debeLanzarNotFoundException() {
        when(specialistRepository.findSpecialistById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> specialistService.findById(99L))
                .isInstanceOf(SpecialistNotFoundException.class);
    }

    @Test
    void create_cuandoDniDuplicado_debeLanzarIllegalArgumentException() {
        when(specialistRepository.findSpecialistByDni("22334455"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> specialistService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DNI");
    }

    @Test
    void create_cuandoDatosValidos_debeRetornarEspecialistaCreado() {
        when(specialistRepository.findSpecialistByDni("22334455")).thenReturn(Optional.empty());
        doNothing().when(specialistRepository).persist(any(Specialist.class));

        SpecialistResponse result = specialistService.create(request);

        assertThat(result.dni()).isEqualTo("22334455");
        assertThat(result.speciality()).isEqualTo(Speciality.CARDIOLOGIA);
        verify(specialistRepository).persist(any(Specialist.class));
    }

    @Test
    void update_cuandoDniCambiado_yYaExiste_debeLanzarConflicto() {
        SpecialistRequest requestConDniNuevo = new SpecialistRequest(
                "Laura", "Gómez", "99887766", "laura@email.com",
                Speciality.NEUROLOGIA, "Av. Corrientes 1234"
        );
        Specialist otraPersona = new Specialist();
        otraPersona.setDni("99887766");

        when(specialistRepository.findSpecialistById(1L)).thenReturn(Optional.of(specialist));
        when(specialistRepository.findSpecialistByDni("99887766"))
                .thenReturn(Optional.of(otraPersona));

        assertThatThrownBy(() -> specialistService.update(1L, requestConDniNuevo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DNI");
    }

    @Test
    void delete_cuandoNoExiste_debeLanzarNotFoundException() {
        when(specialistRepository.findSpecialistById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> specialistService.delete(99L))
                .isInstanceOf(SpecialistNotFoundException.class);

        verify(specialistRepository, never()).delete(any());
    }
}