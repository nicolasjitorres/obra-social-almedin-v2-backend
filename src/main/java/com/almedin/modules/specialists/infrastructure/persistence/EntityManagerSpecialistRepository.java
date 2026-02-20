package com.almedin.modules.specialists.infrastructure.persistence;

import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntityManagerSpecialistRepository implements SpecialistRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<Specialist> findById(Long id) {
        return em.createQuery(
                        "SELECT s FROM Specialist s WHERE s.id = :id AND s.active = true", Specialist.class
                ).setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Specialist> findByDni(String dni) {
        return em.createQuery("SELECT s FROM Specialist s WHERE s.dni = :dni AND s.active = true", Specialist.class)
                .setParameter("dni", dni)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Specialist> findByEmail(String email) {
        return em.createQuery(
                        "SELECT s FROM Specialist s WHERE s.email = :email",
                        Specialist.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void persist(Specialist specialist) {
        em.persist(specialist);
    }

    @Override
    public void deactivate(Specialist specialist) {
        specialist.deactivate();
        em.merge(specialist);
    }

    @Override
    public List<Specialist> listAll(int page, int size, String speciality, boolean includeInactive) {
        String condition1 = includeInactive ? "" : " AND s.active = true";
        String condition2 = includeInactive ? "" : " WHERE s.active = true";
        String jpql = speciality != null
                ? "SELECT s FROM Specialist s WHERE s.speciality = :speciality " + condition1
                : "SELECT s FROM Specialist s " + condition2;

        var query = em.createQuery(jpql, Specialist.class);
        if (speciality != null) query.setParameter("speciality", speciality);

        return query
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countAll(String speciality, boolean includeInactive) {
        String condition1 = includeInactive ? "" : " AND s.active = true";
        String condition2 = includeInactive ? "" : " WHERE s.active = true";
        String jpql = speciality != null
                ? "SELECT COUNT(s) FROM Specialist s WHERE s.speciality = :speciality " + condition1
                : "SELECT COUNT(s) FROM Specialist s " + condition2;

        var query = em.createQuery(jpql, Long.class);
        if (speciality != null) query.setParameter("speciality", speciality);

        return query.getSingleResult();
    }

    @Override
    public void update(Specialist specialist) {
        em.merge(specialist);
    }
}