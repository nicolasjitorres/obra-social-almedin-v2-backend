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
    public List<Specialist> listAllSpecialists() {
        return em.createQuery("SELECT s FROM Specialist s", Specialist.class).getResultList();
    }

    @Override
    public Optional<Specialist> findSpecialistById(Long id) {
        return Optional.ofNullable(em.find(Specialist.class, id));
    }

    @Override
    public Optional<Specialist> findSpecialistByDni(String dni) {
        return em.createQuery("SELECT s FROM Specialist s WHERE s.dni = :dni", Specialist.class)
                .setParameter("dni", dni)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void persist(Specialist specialist) {
        em.persist(specialist);
    }

    @Override
    public void delete(Specialist specialist) {
        em.remove(em.contains(specialist) ? specialist : em.merge(specialist));
    }
}