package com.almedin.modules.scheduling.infrastructure.persistence;

import com.almedin.modules.scheduling.domain.model.SpecialistUnavailability;
import com.almedin.modules.scheduling.domain.repository.UnavailabilityRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntityManagerUnavailabilityRepository implements UnavailabilityRepository {

    @Inject
    EntityManager em;

    @Override
    public List<SpecialistUnavailability> listAll() {
        return em.createQuery("SELECT u FROM SpecialistUnavailability u", SpecialistUnavailability.class)
                .getResultList();
    }

    @Override
    public Optional<SpecialistUnavailability> findById(Long id) {
        return Optional.ofNullable(em.find(SpecialistUnavailability.class, id));
    }

    @Override
    public List<SpecialistUnavailability> findBySpecialistId(Long specialistId) {
        return em.createQuery(
                        "SELECT u FROM SpecialistUnavailability u WHERE u.specialist.id = :id", SpecialistUnavailability.class)
                .setParameter("id", specialistId)
                .getResultList();
    }

    @Override
    public List<SpecialistUnavailability> findBySpecialistIdAndDate(Long specialistId, LocalDate date) {
        return em.createQuery(
                        "SELECT u FROM SpecialistUnavailability u WHERE u.specialist.id = :id " +
                                "AND u.dateFrom <= :date AND (u.dateTo IS NULL OR u.dateTo >= :date)",
                        SpecialistUnavailability.class)
                .setParameter("id", specialistId)
                .setParameter("date", date)
                .getResultList();
    }

    @Override
    public void persist(SpecialistUnavailability unavailability) {
        em.persist(unavailability);
    }

    @Override
    public void delete(SpecialistUnavailability unavailability) {
        em.remove(em.contains(unavailability) ? unavailability : em.merge(unavailability));
    }
}