package com.almedin.modules.scheduling.infrastructure.persistence;

import com.almedin.modules.scheduling.domain.model.AffiliatePenalty;
import com.almedin.modules.scheduling.domain.repository.PenaltyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntityManagerPenaltyRepository implements PenaltyRepository {

    @Inject
    EntityManager em;

    @Override
    public List<AffiliatePenalty> findByAffiliateId(Long affiliateId) {
        return em.createQuery(
                        "SELECT p FROM AffiliatePenalty p WHERE p.affiliate.id = :id", AffiliatePenalty.class)
                .setParameter("id", affiliateId)
                .getResultList();
    }

    @Override
    public List<AffiliatePenalty> findActiveByAffiliateId(Long affiliateId) {
        return em.createQuery(
                        "SELECT p FROM AffiliatePenalty p WHERE p.affiliate.id = :id AND p.active = true",
                        AffiliatePenalty.class)
                .setParameter("id", affiliateId)
                .getResultList();
    }

    @Override
    public long countActiveByAffiliateId(Long affiliateId) {
        return em.createQuery(
                        "SELECT COUNT(p) FROM AffiliatePenalty p WHERE p.affiliate.id = :id AND p.active = true",
                        Long.class)
                .setParameter("id", affiliateId)
                .getSingleResult();
    }

    @Override
    public Optional<AffiliatePenalty> findActiveSuspensionByAffiliateId(Long affiliateId) {
        return em.createQuery(
                        "SELECT p FROM AffiliatePenalty p WHERE p.affiliate.id = :id " +
                                "AND p.active = true AND p.suspendedUntil IS NOT NULL",
                        AffiliatePenalty.class)
                .setParameter("id", affiliateId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void persist(AffiliatePenalty penalty) {
        em.persist(penalty);
    }
}