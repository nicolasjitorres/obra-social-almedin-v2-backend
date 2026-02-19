package com.almedin.modules.scheduling.application.service;

import com.almedin.modules.scheduling.application.dto.UnavailabilityRequest;
import com.almedin.modules.scheduling.application.dto.UnavailabilityResponse;
import com.almedin.modules.scheduling.application.mapper.SchedulingMapper;
import com.almedin.modules.scheduling.domain.model.SpecialistUnavailability;
import com.almedin.modules.scheduling.domain.repository.UnavailabilityRepository;
import com.almedin.modules.shared.application.security.SecurityContext;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class UnavailabilityService {

    @Inject
    UnavailabilityRepository unavailabilityRepository;

    @Inject
    SpecialistRepository specialistRepository;

    @Inject
    SchedulingMapper mapper;

    @Inject
    SecurityContext securityContext;

    public List<UnavailabilityResponse> findBySpecialist(Long specialistId) {
        securityContext.requireSelfOrAdmin(specialistId);
        return mapper.toUnavailabilityResponseList(
                unavailabilityRepository.findBySpecialistId(specialistId));
    }

    @Transactional
    public List<UnavailabilityResponse> create(UnavailabilityRequest request) {
        if (!securityContext.isAdmin()) {
            securityContext.requireSelfOrAdmin(request.specialistId()); // ← agregar
        }
        Specialist specialist = specialistRepository.findById(request.specialistId())
                .orElseThrow(() -> new IllegalArgumentException("Especialista no encontrado con id: " + request.specialistId()));

        if (request.dateTo() != null && request.dateTo().isBefore(request.dateFrom())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        if (request.startTime() != null && request.endTime() != null
                && request.endTime().isBefore(request.startTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Si hay rango de fechas, crear un registro por cada día
        List<SpecialistUnavailability> created = new java.util.ArrayList<>();

        if (request.dateTo() != null) {
            java.time.LocalDate current = request.dateFrom();
            while (!current.isAfter(request.dateTo())) {
                SpecialistUnavailability u = mapper.toUnavailabilityEntity(request);
                u.setSpecialist(specialist);
                u.setDateFrom(current);
                u.setDateTo(current);
                unavailabilityRepository.persist(u);
                created.add(u);
                current = current.plusDays(1);
            }
        } else {
            SpecialistUnavailability u = mapper.toUnavailabilityEntity(request);
            u.setSpecialist(specialist);
            unavailabilityRepository.persist(u);
            created.add(u);
        }

        return mapper.toUnavailabilityResponseList(created);
    }

    @Transactional
    public void delete(Long id) {
        SpecialistUnavailability u = unavailabilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro de no disponibilidad no encontrado con id: " + id));
        if (!securityContext.isAdmin()) {
            securityContext.requireSelfOrAdmin(u.getSpecialist().getId());
        }
        unavailabilityRepository.delete(u);
    }
}