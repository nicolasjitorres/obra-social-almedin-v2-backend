package com.almedin.modules.scheduling.domain.repository;

import com.almedin.modules.scheduling.domain.model.Appointment;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {

    Optional<Appointment> findById(Long id);
    List<Appointment> findBySpecialistIdAndDate(Long specialistId, LocalDate date);
    List<Appointment> findByAffiliateIdAndStatus(Long affiliateId, AppointmentStatus status);
    List<Appointment> findBySpecialistIdAndDateRangeAndStatus(Long specialistId, LocalDate from, LocalDate to, AppointmentStatus status);
    void persist(Appointment appointment);
    void delete(Appointment appointment);
    List<Appointment> findConfirmedByDateAndReminderNotSent(LocalDate date);
    List<Appointment> listAll(int page, int size, AppointmentStatus status);
    long countAll(AppointmentStatus status);
    List<Appointment> findByAffiliateId(Long affiliateId, int page, int size);
    long countByAffiliateId(Long affiliateId);
    List<Appointment> findBySpecialistId(Long specialistId, int page, int size);
    long countBySpecialistId(Long specialistId);
}