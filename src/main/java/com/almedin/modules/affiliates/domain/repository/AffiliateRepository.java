package com.almedin.modules.affiliates.domain.repository;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public interface AffiliateRepository {

    List<Affiliate> listAll();
    Optional<Affiliate> findById(Long id);
    Optional<Affiliate> findByDni(String dni);
    Optional<Affiliate> findByHealthInsuranceCode(String healthInsuranceCode);
    void persist(Affiliate affiliate);
    void deactivate(Affiliate affiliate);
}