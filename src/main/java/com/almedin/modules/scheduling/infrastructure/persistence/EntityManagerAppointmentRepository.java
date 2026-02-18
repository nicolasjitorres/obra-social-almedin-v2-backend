package com.almedin.modules.scheduling.infrastructure.persistence;

import com.almedin.modules.scheduling.domain.model.Appointment;
import com.almedin.modules.scheduling.domain.repository.AppointmentRepository;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntityManagerAppointmentRepository implements AppointmentRepository {

    @Inject
    EntityManager em;

    @Override
    public List<Appointment> listAll() {
        return em.createQuery("SELECT a FROM Appointment a", Appointment.class).getResultList();
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return Optional.ofNullable(em.find(Appointment.class, id));
    }

    @Override
    public List<Appointment> findByAffiliateId(Long affiliateId) {
        return em.createQuery(
                        "SELECT a FROM Appointment a WHERE a.affiliate.id = :id", Appointment.class)
                .setParameter("id", affiliateId)
                .getResultList();
    }

    @Override
    public List<Appointment> findBySpecialistId(Long specialistId) {
        return em.createQuery(
                        "SELECT a FROM Appointment a WHERE a.specialist.id = :id", Appointment.class)
                .setParameter("id", specialistId)
                .getResultList();
    }

    @Override
    public List<Appointment> findBySpecialistIdAndDate(Long specialistId, LocalDate date) {
        return em.createQuery(
                        "SELECT a FROM Appointment a WHERE a.specialist.id = :id AND a.date = :date", Appointment.class)
                .setParameter("id", specialistId)
                .setParameter("date", date)
                .getResultList();
    }

    @Override
    public List<Appointment> findByAffiliateIdAndStatus(Long affiliateId, AppointmentStatus status) {
        return em.createQuery(
                        "SELECT a FROM Appointment a WHERE a.affiliate.id = :id AND a.status = :status", Appointment.class)
                .setParameter("id", affiliateId)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Appointment> findBySpecialistIdAndDateRangeAndStatus(
            Long specialistId, LocalDate from, LocalDate to, AppointmentStatus status) {
        return em.createQuery(
                        "SELECT a FROM Appointment a WHERE a.specialist.id = :id " +
                                "AND a.date BETWEEN :from AND :to AND a.status = :status", Appointment.class)
                .setParameter("id", specialistId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public void persist(Appointment appointment) {
        em.persist(appointment);
    }

    @Override
    public void delete(Appointment appointment) {
        em.remove(em.contains(appointment) ? appointment : em.merge(appointment));
    }
}