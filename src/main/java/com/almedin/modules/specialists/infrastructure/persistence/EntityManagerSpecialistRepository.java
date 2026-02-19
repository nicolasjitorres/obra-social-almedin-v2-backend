package com.almedin.modules.specialists.infrastructure.persistence;

import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntityManagerSpecialistRepository implements SpecialistRepository {

    @Inject
    EntityManager em;

    @Override
    public List<Specialist> listAll() {
        return em.createQuery("SELECT s FROM Specialist s WHERE s.active = true", Specialist.class).getResultList();
    }

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
}