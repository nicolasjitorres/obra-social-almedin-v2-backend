package com.almedin.modules.scheduling.infrastructure.persistence;

import com.almedin.modules.scheduling.domain.model.Schedule;
import com.almedin.modules.scheduling.domain.repository.ScheduleRepository;
import com.almedin.modules.shared.domain.enums.DayOfWeek;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntityManagerScheduleRepository implements ScheduleRepository {

    @Inject
    EntityManager em;

    @Override
    public List<Schedule> listAll() {
        return em.createQuery("SELECT s FROM Schedule s", Schedule.class).getResultList();
    }

    @Override
    public Optional<Schedule> findById(Long id) {
        return Optional.ofNullable(em.find(Schedule.class, id));
    }

    @Override
    public List<Schedule> findBySpecialistId(Long specialistId) {
        return em.createQuery(
                        "SELECT s FROM Schedule s WHERE s.specialist.id = :id AND s.active = true", Schedule.class)
                .setParameter("id", specialistId)
                .getResultList();
    }

    @Override
    public List<Schedule> findBySpecialistIdAndDayOfWeek(Long specialistId, DayOfWeek dayOfWeek) {
        return em.createQuery(
                        "SELECT s FROM Schedule s WHERE s.specialist.id = :id AND s.dayOfWeek = :day AND s.active = true",
                        Schedule.class)
                .setParameter("id", specialistId)
                .setParameter("day", dayOfWeek)
                .getResultList();
    }

    @Override
    public void persist(Schedule schedule) {
        em.persist(schedule);
    }

    @Override
    public void delete(Schedule schedule) {
        em.remove(em.contains(schedule) ? schedule : em.merge(schedule));
    }
}