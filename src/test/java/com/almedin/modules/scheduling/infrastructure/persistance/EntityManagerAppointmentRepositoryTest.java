package com.almedin.modules.scheduling.infrastructure.persistence;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.scheduling.domain.model.Appointment;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.shared.domain.enums.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class EntityManagerAppointmentRepositoryTest {

    @Inject
    EntityManagerAppointmentRepository repository;

    @Inject
    EntityManager em;

    private Affiliate affiliate;
    private Specialist specialist;

    // Lunes fijo para todos los tests
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 4, 6);

    @BeforeEach
    @Transactional
    void setUp() {
        em.createQuery("DELETE FROM AffiliatePenalty").executeUpdate();
        em.createQuery("DELETE FROM Appointment").executeUpdate();
        em.createQuery("DELETE FROM SpecialistUnavailability").executeUpdate();
        em.createQuery("DELETE FROM Schedule").executeUpdate();
        em.createQuery("DELETE FROM Specialist").executeUpdate();
        em.createQuery("DELETE FROM Affiliate").executeUpdate();

        affiliate = new Affiliate();
        affiliate.setFirstName("Test");
        affiliate.setLastName("Afiliado");
        affiliate.setDni("10203040");
        affiliate.setEmail("test.afiliado@email.com");
        affiliate.setHealthInsuranceCode("OS-TEST");
        affiliate.setRole(Role.AFFILIATE);
        em.persist(affiliate);

        specialist = new Specialist();
        specialist.setFirstName("Test");
        specialist.setLastName("Especialista");
        specialist.setDni("10203041");
        specialist.setEmail("test.especialista@email.com");
        specialist.setSpeciality(Speciality.CARDIOLOGIA);
        specialist.setAddress("Test 123");
        specialist.setRole(Role.SPECIALIST);
        em.persist(specialist);
    }

    private Appointment buildAppointment(LocalTime startTime) {
        return Appointment.builder()
                .affiliate(affiliate)
                .specialist(specialist)
                .date(TEST_DATE)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .durationMinutes(30)
                .type(AppointmentType.CONSULTA)
                .status(AppointmentStatus.CONFIRMADA)
                .penaltyApplied(false)
                .reminderSent(false)
                .build();
    }

    @Test
    @Transactional
    void persist_yListAll_debenFuncionar() {
        repository.persist(buildAppointment(LocalTime.of(9, 0)));

        List<Appointment> result = repository.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAffiliate().getDni()).isEqualTo("10203040");
    }

    @Test
    @Transactional
    void findById_cuandoExiste_debeRetornarCita() {
        Appointment appointment = buildAppointment(LocalTime.of(9, 0));
        repository.persist(appointment);

        Optional<Appointment> result = repository.findById(appointment.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(AppointmentType.CONSULTA);
    }

    @Test
    @Transactional
    void findById_cuandoNoExiste_debeRetornarEmpty() {
        Optional<Appointment> result = repository.findById(9999L);

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    void findByAffiliateId_debeRetornarCitasDelAfiliado() {
        repository.persist(buildAppointment(LocalTime.of(9, 0)));
        repository.persist(buildAppointment(LocalTime.of(9, 30)));

        List<Appointment> result = repository.findByAffiliateId(affiliate.getId());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getAffiliate().getId().equals(affiliate.getId()));
    }

    @Test
    @Transactional
    void findBySpecialistIdAndDate_debeRetornarCitasDelDia() {
        repository.persist(buildAppointment(LocalTime.of(9, 0)));
        repository.persist(buildAppointment(LocalTime.of(9, 30)));

        List<Appointment> result = repository.findBySpecialistIdAndDate(
                specialist.getId(), TEST_DATE);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getDate().equals(TEST_DATE));
    }

    @Test
    @Transactional
    void findBySpecialistIdAndDate_conFechaDistinta_debeRetornarVacio() {
        repository.persist(buildAppointment(LocalTime.of(9, 0)));

        List<Appointment> result = repository.findBySpecialistIdAndDate(
                specialist.getId(), TEST_DATE.plusDays(7));

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    void findByAffiliateIdAndStatus_debeFiltraPorEstado() {
        Appointment confirmada = buildAppointment(LocalTime.of(9, 0));
        Appointment cancelada = buildAppointment(LocalTime.of(9, 30));
        cancelada.setStatus(AppointmentStatus.CANCELADA);

        repository.persist(confirmada);
        repository.persist(cancelada);

        List<Appointment> result = repository.findByAffiliateIdAndStatus(
                affiliate.getId(), AppointmentStatus.CONFIRMADA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMADA);
    }

    @Test
    @Transactional
    void findBySpecialistIdAndDateRangeAndStatus_debeRetornarCitasEnRango() {
        repository.persist(buildAppointment(LocalTime.of(9, 0)));

        List<Appointment> result = repository.findBySpecialistIdAndDateRangeAndStatus(
                specialist.getId(),
                TEST_DATE.minusDays(1),
                TEST_DATE.plusDays(1),
                AppointmentStatus.CONFIRMADA);

        assertThat(result).hasSize(1);
    }

    @Test
    @Transactional
    void delete_debeEliminarCita() {
        Appointment appointment = buildAppointment(LocalTime.of(9, 0));
        repository.persist(appointment);

        repository.delete(appointment);

        assertThat(repository.findById(appointment.getId())).isEmpty();
    }
}