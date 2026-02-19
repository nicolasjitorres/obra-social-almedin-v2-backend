package com.almedin.modules.scheduling.domain.repository;

import com.almedin.modules.scheduling.domain.model.SpecialistUnavailability;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UnavailabilityRepository {

    List<SpecialistUnavailability> listAll();
    Optional<SpecialistUnavailability> findById(Long id);
    List<SpecialistUnavailability> findBySpecialistId(Long specialistId);
    List<SpecialistUnavailability> findBySpecialistIdAndDate(Long specialistId, LocalDate date);
    void persist(SpecialistUnavailability unavailability);
    void delete(SpecialistUnavailability unavailability);
}