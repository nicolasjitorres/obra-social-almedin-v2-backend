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
    public Optional<Appointment> findById(Long id) {
        return Optional.ofNullable(em.find(Appointment.class, id));
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

    @Override
    public List<Appointment> findConfirmedByDateAndReminderNotSent(LocalDate date) {
        return em.createQuery(
                        "SELECT a FROM Appointment a " +
                                "WHERE a.date = :date " +
                                "AND a.status = :status " +
                                "AND a.reminderSent = false",
                        Appointment.class)
                .setParameter("date", date)
                .setParameter("status", AppointmentStatus.CONFIRMADA)
                .getResultList();
    }

    @Override
    public List<Appointment> listAll(int page, int size, AppointmentStatus status) {
        String jpql = status != null
                ? "SELECT a FROM Appointment a WHERE a.status = :status"
                : "SELECT a FROM Appointment a";

        var query = em.createQuery(jpql, Appointment.class);
        if (status != null) query.setParameter("status", status);

        return query
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countAll(AppointmentStatus status) {
        String jpql = status != null
                ? "SELECT COUNT(a) FROM Appointment a WHERE a.status = :status"
                : "SELECT COUNT(a) FROM Appointment a";

        var query = em.createQuery(jpql, Long.class);
        if (status != null) query.setParameter("status", status);

        return query.getSingleResult();
    }

    @Override
    public List<Appointment> findByAffiliateId(Long affiliateId, int page, int size) {
        return em.createQuery(
                        "SELECT a FROM Appointment a WHERE a.affiliate.id = :id ORDER BY a.date DESC, a.startTime DESC",
                        Appointment.class)
                .setParameter("id", affiliateId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countByAffiliateId(Long affiliateId) {
        return em.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.affiliate.id = :id", Long.class)
                .setParameter("id", affiliateId)
                .getSingleResult();
    }

    @Override
    public List<Appointment> findBySpecialistId(Long specialistId, int page, int size) {
        return em.createQuery(
                        "SELECT a FROM Appointment a WHERE a.specialist.id = :id ORDER BY a.date DESC, a.startTime DESC",
                        Appointment.class)
                .setParameter("id", specialistId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countBySpecialistId(Long specialistId) {
        return em.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.specialist.id = :id", Long.class)
                .setParameter("id", specialistId)
                .getSingleResult();
    }
}