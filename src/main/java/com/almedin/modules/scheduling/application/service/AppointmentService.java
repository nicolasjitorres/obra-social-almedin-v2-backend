package com.almedin.modules.scheduling.application.service;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import com.almedin.modules.notifications.application.service.NotificationService;
import com.almedin.modules.notifications.domain.model.NotificationEvent;
import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.application.mapper.SchedulingMapper;
import com.almedin.modules.scheduling.domain.exceptions.AppointmentNotFoundException;
import com.almedin.modules.scheduling.domain.model.*;
import com.almedin.modules.scheduling.domain.repository.*;
import com.almedin.modules.shared.application.dto.PageRequest;
import com.almedin.modules.shared.application.dto.PageResponse;
import com.almedin.modules.shared.application.security.SecurityContext;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;
import com.almedin.modules.shared.domain.enums.AppointmentType;
import com.almedin.modules.shared.domain.enums.CancelledBy;
import com.almedin.modules.shared.domain.enums.DayOfWeek;
import com.almedin.modules.shared.domain.exceptions.BusinessRuleViolationException;
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

    @Inject
    SecurityContext securityContext;

    @Inject
    NotificationService notificationService;

    public List<AppointmentResponse> findBySpecialistAndDate(Long specialistId, LocalDate date) {
        securityContext.requireSelfOrAdmin(specialistId);
        return mapper.toAppointmentResponseList(
                appointmentRepository.findBySpecialistIdAndDate(specialistId, date));
    }

    public AppointmentResponse findById(Long id) {
        return mapper.toAppointmentResponse(getOrThrow(id));
    }

    // FUNDAMENTALES LOS COMENTARIOS AQUI PARA ENTENDER LA LOGICA DE NEGOCIO DE CADA VALIDACION, y ademas para entender el funcionamiento
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

        if (!securityContext.isAdmin()) {
            String role = securityContext.getCurrentRole();

            if (role.equals("AFFILIATE")) {
                // El afiliado solo puede crear citas de tipo CONSULTA
                if (request.type() != AppointmentType.CONSULTA) {
                    throw new IllegalArgumentException("Los afiliados solo pueden solicitar turnos de tipo CONSULTA");
                }
                // Y solo para sí mismo
                securityContext.requireSelfOrAdmin(request.affiliateId());

            } else if (role.equals("SPECIALIST")) {
                // El especialista solo puede crear citas derivadas (con parentAppointmentId)
                if (request.parentAppointmentId() == null) {
                    throw new IllegalArgumentException("Los especialistas solo pueden crear turnos derivados de una consulta existente");
                }
                // Y solo para sus propios pacientes
                securityContext.requireSelfOrAdmin(request.specialistId());
            }
        }

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
            throw new BusinessRuleViolationException("El especialista no está disponible en esa fecha");
        }

        // Verificar que el turno no este ya ocupado
        boolean slotOccupied = appointmentRepository
                .findBySpecialistIdAndDate(specialist.getId(), request.date())
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELADA)
                .anyMatch(a -> a.getStartTime().equals(request.startTime()));

        if (slotOccupied) {
            throw new BusinessRuleViolationException("El horario seleccionado ya está ocupado");
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

        notificationService.notify(specialist.getId(), new NotificationEvent(
                "NEW_APPOINTMENT",
                specialist.getId(),
                "Nuevo turno: " + affiliate.getFirstName() + " " + affiliate.getLastName() +
                        " — " + request.date() + " " + request.startTime(),
                java.time.LocalDateTime.now().toString()
        ));

        return mapper.toAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(Long id, CancelAppointmentRequest request) {
        Appointment appointment = getOrThrow(id);

        if (!securityContext.isAdmin()) {
            Long currentUserId = securityContext.getCurrentUserId();
            boolean isOwner = currentUserId.equals(appointment.getAffiliate().getId())
                    || currentUserId.equals(appointment.getSpecialist().getId());
            if (!isOwner) {
                throw new io.quarkus.security.UnauthorizedException("No tenés permiso para cancelar esta cita");
            }
        }

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

        if (request.cancelledBy() == CancelledBy.AFFILIATE) {
            notificationService.notify(appointment.getSpecialist().getId(), new NotificationEvent(
                    "CANCELLED",
                    appointment.getSpecialist().getId(),
                    "Turno cancelado: " + appointment.getAffiliate().getFirstName() +
                            " " + appointment.getAffiliate().getLastName() +
                            " — " + appointment.getDate() + " " + appointment.getStartTime(),
                    java.time.LocalDateTime.now().toString()
            ));
        }

        return mapper.toAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse complete(Long id, CompleteAppointmentRequest request) {
        Appointment appointment = getOrThrow(id);
        securityContext.requireSelfOrAdmin(appointment.getSpecialist().getId());

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
        securityContext.requireSelfOrAdmin(appointment.getSpecialist().getId());

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

    private DayOfWeek mapDayOfWeek(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> DayOfWeek.LUNES;
            case TUESDAY -> DayOfWeek.MARTES;
            case WEDNESDAY -> DayOfWeek.MIERCOLES;
            case THURSDAY -> DayOfWeek.JUEVES;
            case FRIDAY -> DayOfWeek.VIERNES;
            case SATURDAY -> DayOfWeek.SABADO;
            case SUNDAY -> DayOfWeek.DOMINGO;
        };
    }

    private Appointment getOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    public PageResponse<AppointmentResponse> findAll(PageRequest pageRequest, AppointmentStatus status) {
        List<AppointmentResponse> content = mapper.toAppointmentResponseList(
                appointmentRepository.listAll(pageRequest.page(), pageRequest.size(), status)
        );
        long total = appointmentRepository.countAll(status);
        return PageResponse.of(content, pageRequest, total);
    }

    public PageResponse<AppointmentResponse> findByAffiliate(Long affiliateId, PageRequest pageRequest) {
        securityContext.requireSelfOrAdmin(affiliateId);
        List<AppointmentResponse> content = mapper.toAppointmentResponseList(
                appointmentRepository.findByAffiliateId(affiliateId, pageRequest.page(), pageRequest.size())
        );
        long total = appointmentRepository.countByAffiliateId(affiliateId);
        return PageResponse.of(content, pageRequest, total);
    }

    public PageResponse<AppointmentResponse> findBySpecialist(Long specialistId, PageRequest pageRequest) {
        securityContext.requireSelfOrAdmin(specialistId);
        List<AppointmentResponse> content = mapper.toAppointmentResponseList(
                appointmentRepository.findBySpecialistId(specialistId, pageRequest.page(), pageRequest.size())
        );
        long total = appointmentRepository.countBySpecialistId(specialistId);
        return PageResponse.of(content, pageRequest, total);
    }
}