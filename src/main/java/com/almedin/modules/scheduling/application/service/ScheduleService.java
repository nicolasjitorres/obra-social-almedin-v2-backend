package com.almedin.modules.scheduling.application.service;

import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.application.mapper.SchedulingMapper;
import com.almedin.modules.scheduling.domain.model.Schedule;
import com.almedin.modules.scheduling.domain.repository.AppointmentRepository;
import com.almedin.modules.scheduling.domain.repository.ScheduleRepository;
import com.almedin.modules.shared.application.security.SecurityContext;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;
import com.almedin.modules.shared.domain.enums.CancelledBy;
import com.almedin.modules.shared.domain.enums.DayOfWeek;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ScheduleService {

    @Inject
    ScheduleRepository scheduleRepository;

    @Inject
    AppointmentRepository appointmentRepository;

    @Inject
    SpecialistRepository specialistRepository;

    @Inject
    SchedulingMapper mapper;

    @Inject
    SecurityContext securityContext;

    public List<ScheduleResponse> findBySpecialist(Long specialistId) {
        return mapper.toScheduleResponseList(scheduleRepository.findBySpecialistId(specialistId));
    }

    @Transactional
    public ScheduleResponse create(ScheduleRequest request) {

        if (!securityContext.isAdmin()) {
            securityContext.requireSelfOrAdmin(request.specialistId());
        }

        Specialist specialist = specialistRepository.findById(request.specialistId())
                .orElseThrow(() -> new IllegalArgumentException("Especialista no encontrado con id: " + request.specialistId()));

        if (request.endTime().isBefore(request.startTime()) || request.endTime().equals(request.startTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        if (request.slotDuration() <= 0) {
            throw new IllegalArgumentException("La duración del slot debe ser mayor a 0");
        }

        Schedule schedule = mapper.toScheduleEntity(request);
        schedule.setSpecialist(specialist);
        schedule.setActive(true);
        scheduleRepository.persist(schedule);
        return mapper.toScheduleResponse(schedule);
    }

    @Transactional
    public ScheduleResponse update(Long id, ScheduleRequest request) {
        Schedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con id: " + id));

        if (!securityContext.isAdmin()) {
            securityContext.requireSelfOrAdmin(existing.getSpecialist().getId()); // ← agregar
        }

        if (request.endTime().isBefore(request.startTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        LocalDate today = LocalDate.now();

        // Buscar citas CONFIRMADAS o PENDIENTES del especialista para ese día de semana
        // que queden fuera del nuevo rango horario y cancelarlas automáticamente
        appointmentRepository.findBySpecialistIdAndDateRangeAndStatus(
                        existing.getSpecialist().getId(), today, today.plusMonths(3), AppointmentStatus.CONFIRMADA)
                .stream()
                .filter(a -> a.getDate().getDayOfWeek().name().equals(request.dayOfWeek().name()))
                .filter(a -> a.getStartTime().isBefore(request.startTime()) || a.getStartTime().isAfter(request.endTime()))
                .forEach(a -> {
                    a.setStatus(AppointmentStatus.CANCELADA);
                    a.setCancelledBy(CancelledBy.SYSTEM);
                    a.setCancellationReason("Horario del especialista actualizado. La cita quedó fuera del nuevo rango horario.");
                });

        existing.setDayOfWeek(request.dayOfWeek());
        existing.setStartTime(request.startTime());
        existing.setEndTime(request.endTime());
        existing.setSlotDuration(request.slotDuration());

        return mapper.toScheduleResponse(existing);
    }

    @Transactional
    public void deactivate(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con id: " + id));
        if (!securityContext.isAdmin()) {
            securityContext.requireSelfOrAdmin(schedule.getSpecialist().getId()); // ← agregar
        }
        schedule.setActive(false);
    }

    public List<AvailableSlotResponse> getAvailableSlots(Long specialistId, LocalDate date) {

        DayOfWeek dayOfWeek = DayOfWeek.from(date.getDayOfWeek());

        // Buscar horarios activos del especialista para ese día
        List<Schedule> schedules = scheduleRepository.findBySpecialistIdAndDayOfWeek(specialistId, dayOfWeek);

        if (schedules.isEmpty()) return List.of();

        // Obtener citas ya ocupadas ese día
        List<LocalTime> occupiedSlots = appointmentRepository
                .findBySpecialistIdAndDate(specialistId, date)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELADA)
                .map(a -> a.getStartTime())
                .toList();

        List<AvailableSlotResponse> available = new ArrayList<>();

        for (Schedule schedule : schedules) {
            LocalTime current = schedule.getStartTime();
            while (current.plusMinutes(schedule.getSlotDuration()).compareTo(schedule.getEndTime()) <= 0) {
                if (!occupiedSlots.contains(current)) {
                    available.add(new AvailableSlotResponse(
                            current,
                            current.plusMinutes(schedule.getSlotDuration()),
                            schedule.getSlotDuration()
                    ));
                }
                current = current.plusMinutes(schedule.getSlotDuration());
            }
        }

        return available;
    }
}