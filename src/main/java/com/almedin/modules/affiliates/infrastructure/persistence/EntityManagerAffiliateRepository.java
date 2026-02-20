package com.almedin.modules.affiliates.infrastructure.persistence;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntityManagerAffiliateRepository implements AffiliateRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<Affiliate> findById(Long id) {
        return em.createQuery(
                        "SELECT a FROM Affiliate a WHERE a.id = :id AND a.active = true", Affiliate.class
                ).setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void persist(Affiliate affiliate) {
        em.persist(affiliate);
    }

    @Override
    public void deactivate(Affiliate affiliate) {
        affiliate.deactivate();
        em.merge(affiliate);
    }

    @Override
    public Optional<Affiliate> findByDni(String dni) {
        return em.createQuery(
                        "SELECT a FROM Affiliate a WHERE a.dni = :dni AND a.active = true", Affiliate.class)
                .setParameter("dni", dni)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Affiliate> findByEmail(String email) {
        return em.createQuery(
                        "SELECT a FROM Affiliate a WHERE a.email = :email",
                        Affiliate.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Affiliate> findByHealthInsuranceCode(String healthInsuranceCode) {
        return em.createQuery(
                        "SELECT a FROM Affiliate a WHERE a.healthInsuranceCode = :code AND a.active = true", Affiliate.class)
                .setParameter("code", healthInsuranceCode)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Affiliate> listAll(int page, int size, boolean includeInactive) {
        String condition = includeInactive ? "" : " WHERE a.active = true";
        return em.createQuery("SELECT a FROM Affiliate a" + condition, Affiliate.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countAll(boolean includeInactive) {
        String condition = includeInactive ? "" : " WHERE a.active = true";
        return em.createQuery("SELECT COUNT(a) FROM Affiliate a" + condition, Long.class)
                .getSingleResult();
    }

}