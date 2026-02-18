package com.almedin.modules.scheduling.application.service;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.application.mapper.SchedulingMapper;
import com.almedin.modules.scheduling.domain.model.*;
import com.almedin.modules.scheduling.domain.repository.*;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;
import com.almedin.modules.shared.domain.enums.CancelledBy;
import com.almedin.modules.shared.domain.enums.DayOfWeek;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ApplicationScoped
public class AppointmentService {

    private static final int PENALTY_THRESHOLD = 3;
    private static final int CANCELLATION_HOURS_LIMIT = 2;
    private static final int SUSPENSION_DAYS = 30;

    @Inject
    AppointmentRepository appointmentRepository;

    @Inject
    ScheduleRepository scheduleRepository;

    @Inject
    UnavailabilityRepository unavailabilityRepository;

    @Inject
    PenaltyRepository penaltyRepository;

    @Inject
    AffiliateRepository affiliateRepository;

    @Inject
    SpecialistRepository specialistRepository;

    @Inject
    SchedulingMapper mapper;

    public List<AppointmentResponse> findByAffiliate(Long affiliateId) {
        return mapper.toAppointmentResponseList(
                appointmentRepository.findByAffiliateId(affiliateId));
    }

    public List<AppointmentResponse> findBySpecialist(Long specialistId) {
        return mapper.toAppointmentResponseList(
                appointmentRepository.findBySpecialistId(specialistId));
    }

    public List<AppointmentResponse> findBySpecialistAndDate(Long specialistId, LocalDate date) {
        return mapper.toAppointmentResponseList(
                appointmentRepository.findBySpecialistIdAndDate(specialistId, date));
    }

    public AppointmentResponse findById(Long id) {
        return mapper.toAppointmentResponse(getOrThrow(id));
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        Affiliate affiliate = affiliateRepository.findById(request.affiliateId())
                .orElseThrow(() -> new IllegalArgumentException("Afiliado no encontrado con id: " + request.affiliateId()));

        Specialist specialist = specialistRepository.findById(request.specialistId())
                .orElseThrow(() -> new IllegalArgumentException("Especialista no encontrado con id: " + request.specialistId()));

        // Verificar que el afiliado no esta suspendido
        penaltyRepository.findActiveSuspensionByAffiliateId(affiliate.getId())
                .ifPresent(p -> {
                    throw new IllegalStateException(
                            "El afiliado tiene una suspensión activa hasta: " + p.getSuspendedUntil());
                });

        // Verificar que el especialista tiene horario ese día
        DayOfWeek dayOfWeek = DayOfWeek.from(request.date().getDayOfWeek());
        List<Schedule> schedules = scheduleRepository.findBySpecialistIdAndDayOfWeek(
                specialist.getId(), dayOfWeek);

        if (schedules.isEmpty()) {
            throw new IllegalArgumentException("El especialista no tiene horario disponible ese día");
        }

        // Verificar que el turno solicitado esta dentro del horario
        Schedule matchingSchedule = schedules.stream()
                .filter(s -> !request.startTime().isBefore(s.getStartTime())
                        && request.startTime().plusMinutes(s.getSlotDuration())
                        .compareTo(s.getEndTime()) <= 0)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El horario solicitado no está disponible para este especialista"));

        // Verificar que el especialista no esta marcado como no disponible ese día
        boolean isUnavailable = !unavailabilityRepository
                .findBySpecialistIdAndDate(specialist.getId(), request.date()).isEmpty();
        if (isUnavailable) {
            throw new IllegalArgumentException("El especialista no está disponible en esa fecha");
        }

        // Verificar que el turno no este ya ocupado
        boolean slotOccupied = appointmentRepository
                .findBySpecialistIdAndDate(specialist.getId(), request.date())
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELADA)
                .anyMatch(a -> a.getStartTime().equals(request.startTime()));

        if (slotOccupied) {
            throw new IllegalArgumentException("El horario seleccionado ya está ocupado");
        }

        LocalTime endTime = request.startTime().plusMinutes(matchingSchedule.getSlotDuration());

        // Manejar cita derivada
        Appointment parent = null;
        Integer duration = matchingSchedule.getSlotDuration();

        if (request.parentAppointmentId() != null) {
            parent = getOrThrow(request.parentAppointmentId());
            // Para citas derivadas, la duracion la calcula el especialista
            duration = matchingSchedule.getSlotDuration();
        }

        Appointment appointment = Appointment.builder()
                .affiliate(affiliate)
                .specialist(specialist)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(endTime)
                .durationMinutes(duration)
                .type(request.type())
                .status(AppointmentStatus.CONFIRMADA)
                .penaltyApplied(false)
                .reminderSent(false)
                .parentAppointment(parent)
                .build();

        appointmentRepository.persist(appointment);
        return mapper.toAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(Long id, CancelAppointmentRequest request) {
        Appointment appointment = getOrThrow(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELADA) {
            throw new IllegalStateException("La cita ya está cancelada");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETADA) {
            throw new IllegalStateException("No se puede cancelar una cita ya completada");
        }

        // Verificar si aplica multa (solo cuando cancela el afiliado)
        boolean applyPenalty = false;
        if (request.cancelledBy() == CancelledBy.AFFILIATE) {
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointment.getDate(), appointment.getStartTime());
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilAppointment = java.time.Duration.between(now, appointmentDateTime).toHours();

            if (hoursUntilAppointment < CANCELLATION_HOURS_LIMIT) {
                applyPenalty = true;
                applyPenaltyToAffiliate(appointment);
            }
        }

        appointment.setStatus(AppointmentStatus.CANCELADA);
        appointment.setCancelledBy(request.cancelledBy());
        appointment.setCancellationReason(request.reason());
        appointment.setPenaltyApplied(applyPenalty);

        return mapper.toAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse complete(Long id, CompleteAppointmentRequest request) {
        Appointment appointment = getOrThrow(id);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMADA) {
            throw new IllegalStateException("Solo se pueden completar citas confirmadas");
        }

        appointment.setStatus(AppointmentStatus.COMPLETADA);
        appointment.setClinicalNotes(request.clinicalNotes());
        appointment.setPrescription(request.prescription());

        return mapper.toAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse markAsAbsent(Long id) {
        Appointment appointment = getOrThrow(id);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMADA) {
            throw new IllegalStateException("Solo se pueden marcar como ausente citas confirmadas");
        }

        appointment.setStatus(AppointmentStatus.AUSENTE);
        appointment.setPenaltyApplied(true);
        applyPenaltyToAffiliate(appointment);

        return mapper.toAppointmentResponse(appointment);
    }

    private void applyPenaltyToAffiliate(Appointment appointment) {
        AffiliatePenalty penalty = AffiliatePenalty.builder()
                .affiliate(appointment.getAffiliate())
                .appointment(appointment)
                .active(true)
                .build();

        long activePenalties = penaltyRepository.countActiveByAffiliateId(
                appointment.getAffiliate().getId());

        // En la tercera multa se aplica suspensión de 30 días
        if (activePenalties + 1 >= PENALTY_THRESHOLD) {
            penalty.setSuspendedUntil(LocalDateTime.now().plusDays(SUSPENSION_DAYS));
        }

        penaltyRepository.persist(penalty);
    }

    private com.almedin.modules.shared.domain.enums.DayOfWeek mapDayOfWeek(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> com.almedin.modules.shared.domain.enums.DayOfWeek.LUNES;
            case TUESDAY -> com.almedin.modules.shared.domain.enums.DayOfWeek.MARTES;
            case WEDNESDAY -> com.almedin.modules.shared.domain.enums.DayOfWeek.MIERCOLES;
            case THURSDAY -> com.almedin.modules.shared.domain.enums.DayOfWeek.JUEVES;
            case FRIDAY -> com.almedin.modules.shared.domain.enums.DayOfWeek.VIERNES;
            case SATURDAY -> com.almedin.modules.shared.domain.enums.DayOfWeek.SABADO;
            case SUNDAY -> com.almedin.modules.shared.domain.enums.DayOfWeek.DOMINGO;
        };
    }

    private Appointment getOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con id: " + id));
    }
}