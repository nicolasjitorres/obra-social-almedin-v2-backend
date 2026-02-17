package com.almedin.modules.affiliates.domain.repository;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public interface AffiliateRepository {

    List<Affiliate> listAllAffiliates();
    Optional<Affiliate> findAffiliateById(Long id);
    void persist(Affiliate affiliate);
    void delete(Affiliate affiliate);
    Optional<Affiliate> findAffiliateByDni(String dni);
    Optional<Affiliate> findAffiliateByHealthInsuranceCode(String healthInsuranceCode);
}