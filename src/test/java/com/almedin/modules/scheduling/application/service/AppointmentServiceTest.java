package com.almedin.modules.scheduling.application.service;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.domain.model.*;
import com.almedin.modules.scheduling.domain.repository.*;
import com.almedin.modules.shared.domain.enums.*;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class AppointmentServiceTest {

    @InjectMock
    AppointmentRepository appointmentRepository;

    @InjectMock
    ScheduleRepository scheduleRepository;

    @InjectMock
    UnavailabilityRepository unavailabilityRepository;

    @InjectMock
    PenaltyRepository penaltyRepository;

    @InjectMock
    AffiliateRepository affiliateRepository;

    @InjectMock
    SpecialistRepository specialistRepository;

    @Inject
    AppointmentService appointmentService;

    private Affiliate affiliate;
    private Specialist specialist;
    private Schedule schedule;
    private Appointment appointment;
    private AppointmentRequest request;

    @BeforeEach
    void setUp() {

        affiliate = new Affiliate();
        affiliate.setId(1L);
        affiliate.setFirstName("Juan");
        affiliate.setLastName("Pérez");
        affiliate.setDni("12345678");
        affiliate.setEmail("juan@email.com");

        specialist = new Specialist();
        specialist.setId(1L);
        specialist.setFirstName("Laura");
        specialist.setLastName("Gómez");
        specialist.setDni("22334455");
        specialist.setEmail("laura@email.com");
        specialist.setSpeciality(Speciality.CARDIOLOGIA);

        // Lunes con turnos de 30 minutos de 9 a 13
        schedule = new Schedule();
        schedule.setId(1L);
        schedule.setSpecialist(specialist);
        schedule.setDayOfWeek(DayOfWeek.LUNES);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(13, 0));
        schedule.setSlotDuration(30);
        schedule.setActive(true);

        // Fecha que sea lunes
        LocalDate monday = LocalDate.of(2026, 3, 9);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setAffiliate(affiliate);
        appointment.setSpecialist(specialist);
        appointment.setDate(monday);
        appointment.setStartTime(LocalTime.of(9, 0));
        appointment.setEndTime(LocalTime.of(9, 30));
        appointment.setDurationMinutes(30);
        appointment.setType(AppointmentType.CONSULTA);
        appointment.setStatus(AppointmentStatus.CONFIRMADA);
        appointment.setPenaltyApplied(false);
        appointment.setReminderSent(false);

        request = new AppointmentRequest(
                1L, 1L, monday,
                LocalTime.of(9, 0),
                AppointmentType.CONSULTA,
                null
        );
    }

    @Test
    void create_cuandoDatosValidos_debeCrearCita() {
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(affiliate));
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(penaltyRepository.findActiveSuspensionByAffiliateId(1L)).thenReturn(Optional.empty());
        when(scheduleRepository.findBySpecialistIdAndDayOfWeek(1L, DayOfWeek.LUNES))
                .thenReturn(List.of(schedule));
        when(unavailabilityRepository.findBySpecialistIdAndDate(1L, request.date()))
                .thenReturn(List.of());
        when(appointmentRepository.findBySpecialistIdAndDate(1L, request.date()))
                .thenReturn(List.of());
        doNothing().when(appointmentRepository).persist(any());

        AppointmentResponse result = appointmentService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(AppointmentType.CONSULTA);
        assertThat(result.status()).isEqualTo(AppointmentStatus.CONFIRMADA);
        verify(appointmentRepository).persist(any(Appointment.class));
    }

    @Test
    void create_cuandoAfiliadoSuspendido_debeLanzarException() {
        AffiliatePenalty suspension = new AffiliatePenalty();
        suspension.setSuspendedUntil(LocalDateTime.now().plusDays(15));

        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(affiliate));
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(penaltyRepository.findActiveSuspensionByAffiliateId(1L))
                .thenReturn(Optional.of(suspension));

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspensión activa");

        verify(appointmentRepository, never()).persist(any());
    }

    @Test
    void create_cuandoEspecialistaSinHorarioEseDia_debeLanzarException() {
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(affiliate));
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(penaltyRepository.findActiveSuspensionByAffiliateId(1L)).thenReturn(Optional.empty());
        when(scheduleRepository.findBySpecialistIdAndDayOfWeek(any(), any()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no tiene horario disponible");

        verify(appointmentRepository, never()).persist(any());
    }

    @Test
    void create_cuandoEspecialistaNoDisponible_debeLanzarException() {
        SpecialistUnavailability unavailability = new SpecialistUnavailability();
        unavailability.setDateFrom(request.date());

        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(affiliate));
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(penaltyRepository.findActiveSuspensionByAffiliateId(1L)).thenReturn(Optional.empty());
        when(scheduleRepository.findBySpecialistIdAndDayOfWeek(1L, DayOfWeek.LUNES))
                .thenReturn(List.of(schedule));
        when(unavailabilityRepository.findBySpecialistIdAndDate(1L, request.date()))
                .thenReturn(List.of(unavailability));

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está disponible");

        verify(appointmentRepository, never()).persist(any());
    }

    @Test
    void create_cuandoSlotOcupado_debeLanzarException() {
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(affiliate));
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(penaltyRepository.findActiveSuspensionByAffiliateId(1L)).thenReturn(Optional.empty());
        when(scheduleRepository.findBySpecialistIdAndDayOfWeek(1L, DayOfWeek.LUNES))
                .thenReturn(List.of(schedule));
        when(unavailabilityRepository.findBySpecialistIdAndDate(1L, request.date()))
                .thenReturn(List.of());
        when(appointmentRepository.findBySpecialistIdAndDate(1L, request.date()))
                .thenReturn(List.of(appointment)); // slot 9:00 ya ocupado

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está ocupado");

        verify(appointmentRepository, never()).persist(any());
    }


    @Test
    void cancel_conMasDe2HorasDeAnticipacion_noDebeAplicarMulta() {
        appointment.setDate(LocalDate.now().plusDays(3));
        appointment.setStartTime(LocalTime.of(9, 0));

        CancelAppointmentRequest cancelRequest = new CancelAppointmentRequest(
                CancelledBy.AFFILIATE, "No puedo asistir"
        );

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        AppointmentResponse result = appointmentService.cancel(1L, cancelRequest);

        assertThat(result.status()).isEqualTo(AppointmentStatus.CANCELADA);
        assertThat(result.penaltyApplied()).isFalse();
        verify(penaltyRepository, never()).persist(any());
    }

    @Test
    void cancel_conMenosDe2HorasDeAnticipacion_debeAplicarMulta() {
        // Cita en 30 minutos
        appointment.setDate(LocalDate.now());
        appointment.setStartTime(LocalTime.now().plusMinutes(30));

        CancelAppointmentRequest cancelRequest = new CancelAppointmentRequest(
                CancelledBy.AFFILIATE, "Emergencia"
        );

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(penaltyRepository.countActiveByAffiliateId(1L)).thenReturn(0L);
        doNothing().when(penaltyRepository).persist(any());

        AppointmentResponse result = appointmentService.cancel(1L, cancelRequest);

        assertThat(result.status()).isEqualTo(AppointmentStatus.CANCELADA);
        assertThat(result.penaltyApplied()).isTrue();
        verify(penaltyRepository).persist(any(AffiliatePenalty.class));
    }

    @Test
    void cancel_porEspecialista_nuncaAplicaMulta() {
        appointment.setDate(LocalDate.now());
        appointment.setStartTime(LocalTime.now().plusMinutes(30));

        CancelAppointmentRequest cancelRequest = new CancelAppointmentRequest(
                CancelledBy.SPECIALIST, "No podré atender"
        );

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        AppointmentResponse result = appointmentService.cancel(1L, cancelRequest);

        assertThat(result.status()).isEqualTo(AppointmentStatus.CANCELADA);
        assertThat(result.cancelledBy()).isEqualTo(CancelledBy.SPECIALIST);
        verify(penaltyRepository, never()).persist(any());
    }

    @Test
    void cancel_citaYaCancelada_debeLanzarException() {
        appointment.setStatus(AppointmentStatus.CANCELADA);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(1L,
                new CancelAppointmentRequest(CancelledBy.AFFILIATE, "motivo")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está cancelada");
    }


    @Test
    void cancel_terceraMulta_debeAplicarSuspension() {
        appointment.setDate(LocalDate.now());
        appointment.setStartTime(LocalTime.now().plusMinutes(30));

        CancelAppointmentRequest cancelRequest = new CancelAppointmentRequest(
                CancelledBy.AFFILIATE, "Emergencia"
        );

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        // Ya tiene 2 multas activas — la próxima es la tercera
        when(penaltyRepository.countActiveByAffiliateId(1L)).thenReturn(2L);
        doNothing().when(penaltyRepository).persist(any());

        appointmentService.cancel(1L, cancelRequest);

        verify(penaltyRepository).persist(argThat(penalty ->
                penalty.getSuspendedUntil() != null
        ));
    }


    @Test
    void complete_cuandoCitaConfirmada_debeCompletarConNotas() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        CompleteAppointmentRequest completeRequest = new CompleteAppointmentRequest(
                "Paciente con buena evolución", "Ibuprofeno 400mg"
        );

        AppointmentResponse result = appointmentService.complete(1L, completeRequest);

        assertThat(result.status()).isEqualTo(AppointmentStatus.COMPLETADA);
        assertThat(result.clinicalNotes()).isEqualTo("Paciente con buena evolución");
        assertThat(result.prescription()).isEqualTo("Ibuprofeno 400mg");
    }

    @Test
    void complete_cuandoCitaNoConfirmada_debeLanzarException() {
        appointment.setStatus(AppointmentStatus.CANCELADA);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.complete(1L,
                new CompleteAppointmentRequest("notas", null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden completar citas confirmadas");
    }


    @Test
    void markAsAbsent_debeAplicarMultaYCambiarEstado() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(penaltyRepository.countActiveByAffiliateId(1L)).thenReturn(0L);
        doNothing().when(penaltyRepository).persist(any());

        AppointmentResponse result = appointmentService.markAsAbsent(1L);

        assertThat(result.status()).isEqualTo(AppointmentStatus.AUSENTE);
        assertThat(result.penaltyApplied()).isTrue();
        verify(penaltyRepository).persist(any(AffiliatePenalty.class));
    }
}