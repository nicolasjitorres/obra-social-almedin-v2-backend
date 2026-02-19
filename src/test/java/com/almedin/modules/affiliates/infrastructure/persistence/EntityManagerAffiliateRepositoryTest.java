package com.almedin.modules.affiliates.infrastructure.persistence;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.shared.domain.enums.Role;
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
class EntityManagerAffiliateRepositoryTest {

    @Inject
    EntityManagerAffiliateRepository repository;

    @Inject
    EntityManager em;

    private Affiliate affiliate;

    @BeforeEach
    @Transactional
    void setUp() {
        em.createQuery("DELETE FROM AffiliatePenalty").executeUpdate();
        em.createQuery("DELETE FROM Appointment").executeUpdate();
        em.createQuery("DELETE FROM Affiliate").executeUpdate();

        affiliate = new Affiliate();
        affiliate.setFirstName("María");
        affiliate.setLastName("García");
        affiliate.setDni("87654321");
        affiliate.setEmail("maria@email.com");
        affiliate.setHealthInsuranceCode("OS-002");
        affiliate.setPassword("hashed-password-para-tests");
        affiliate.setRole(Role.AFFILIATE);
    }

    @Test
    @Transactional
    void persist_yListAll_debenFuncionar() {
        repository.persist(affiliate);

        List<Affiliate> result = repository.listAll(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDni()).isEqualTo("87654321");
    }

    @Test
    @Transactional
    void findByDni_cuandoExiste_debeRetornarAfiliado() {
        repository.persist(affiliate);

        Optional<Affiliate> result = repository.findByDni("87654321");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("maria@email.com");
    }

    @Test
    @Transactional
    void findByDni_cuandoNoExiste_debeRetornarEmpty() {
        Optional<Affiliate> result = repository.findByDni("00000000");

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    void findByHealthInsuranceCode_cuandoExiste_debeRetornarAfiliado() {
        repository.persist(affiliate);

        Optional<Affiliate> result = repository.findByHealthInsuranceCode("OS-002");

        assertThat(result).isPresent();
        assertThat(result.get().getDni()).isEqualTo("87654321");
    }

    @Test
    @Transactional
    void deactivate_debeEliminarAfiliado() {
        repository.persist(affiliate);
        Affiliate persisted = repository.findByDni("87654321").get();

        repository.deactivate(persisted);

        assertThat(repository.findByDni("87654321")).isEmpty();
    }
}