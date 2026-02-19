package com.almedin.modules.affiliates.domain.repository;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public interface AffiliateRepository {

    Optional<Affiliate> findById(Long id);
    Optional<Affiliate> findByDni(String dni);
    Optional<Affiliate> findByEmail(String email);
    Optional<Affiliate> findByHealthInsuranceCode(String healthInsuranceCode);
    void persist(Affiliate affiliate);
    void deactivate(Affiliate affiliate);
    List<Affiliate> listAll(int page, int size);
    long countAll();
}