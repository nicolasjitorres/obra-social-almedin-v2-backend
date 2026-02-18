package com.almedin.modules.scheduling.domain.repository;

import com.almedin.modules.scheduling.domain.model.AffiliatePenalty;

import java.util.List;
import java.util.Optional;

public interface PenaltyRepository {

    List<AffiliatePenalty> findByAffiliateId(Long affiliateId);
    List<AffiliatePenalty> findActiveByAffiliateId(Long affiliateId);
    long countActiveByAffiliateId(Long affiliateId);
    Optional<AffiliatePenalty> findActiveSuspensionByAffiliateId(Long affiliateId);
    void persist(AffiliatePenalty penalty);
}