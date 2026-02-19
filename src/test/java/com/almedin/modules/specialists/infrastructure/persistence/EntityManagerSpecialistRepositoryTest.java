package com.almedin.modules.specialists.infrastructure.persistence;

import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.shared.domain.enums.Role;
import com.almedin.modules.shared.domain.enums.Speciality;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class EntityManagerSpecialistRepositoryTest {

    @Inject
    EntityManagerSpecialistRepository repository;

    @Inject
    EntityManager em;

    private Specialist specialist;

    @BeforeEach
    @Transactional
    void setUp() {
        em.createQuery("DELETE FROM AffiliatePenalty").executeUpdate();
        em.createQuery("DELETE FROM SpecialistUnavailability").executeUpdate();
        em.createQuery("DELETE FROM Schedule").executeUpdate();
        em.createQuery("DELETE FROM Appointment").executeUpdate();
        em.createQuery("DELETE FROM Specialist").executeUpdate();

        specialist = new Specialist();
        specialist.setFirstName("Carlos");
        specialist.setLastName("Méndez");
        specialist.setDni("33445566");
        specialist.setEmail("carlos@email.com");
        specialist.setSpeciality(Speciality.NEUROLOGIA);
        specialist.setAddress("Av. San Martín 500");
        specialist.setPassword("hashed-password-para-tests");
        specialist.setRole(Role.SPECIALIST);
    }

    @Test
    @Transactional
    void persist_yListAll_debenFuncionar() {
        repository.persist(specialist);

        List<Specialist> result = repository.listAll(0,10,null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDni()).isEqualTo("33445566");
    }

    @Test
    @Transactional
    void findByDni_cuandoExiste_debeRetornarEspecialista() {
        repository.persist(specialist);

        Optional<Specialist> result = repository.findByDni("33445566");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("carlos@email.com");
    }

    @Test
    @Transactional
    void findByDni_cuandoNoExiste_debeRetornarEmpty() {
        Optional<Specialist> result = repository.findByDni("00000000");

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    void findById_cuandoExiste_debeRetornarEspecialista() {
        repository.persist(specialist);
        Specialist persisted = repository.findByDni("33445566").get();

        Optional<Specialist> result = repository.findById(persisted.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getSpeciality()).isEqualTo(Speciality.NEUROLOGIA);
    }

    @Test
    @Transactional
    void deactivate_debeDesactivarEspecialista() {
        repository.persist(specialist);
        Specialist persisted = repository.findByDni("33445566").get();

        repository.deactivate(persisted);

        assertThat(repository.findByDni("33445566")).isEmpty();
    }
}