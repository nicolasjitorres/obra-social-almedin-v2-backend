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
    public List<Affiliate> listAllAffiliates() {
        return em.createQuery("SELECT a FROM Affiliate a", Affiliate.class)
                .getResultList();
    }

    @Override
    public Optional<Affiliate> findAffiliateById(Long id) {
        return Optional.ofNullable(em.find(Affiliate.class, id));
    }

    @Override
    public void persist(Affiliate affiliate) {
        em.persist(affiliate);
    }

    @Override
    public void delete(Affiliate affiliate) {
        Affiliate managed = em.contains(affiliate)
                ? affiliate
                : em.merge(affiliate);
        em.remove(managed);
    }

    @Override
    public Optional<Affiliate> findAffiliateByDni(String dni) {
        return em.createQuery(
                        "SELECT a FROM Affiliate a WHERE a.dni = :dni", Affiliate.class)
                .setParameter("dni", dni)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Affiliate> findAffiliateByHealthInsuranceCode(String healthInsuranceCode) {
        return em.createQuery(
                        "SELECT a FROM Affiliate a WHERE a.healthInsuranceCode = :code", Affiliate.class)
                .setParameter("code", healthInsuranceCode)
                .getResultStream()
                .findFirst();
    }
}