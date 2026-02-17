package com.almedin.modules.affiliates.domain.repository;

import com.almedin.modules.affiliates.domain.model.Affiliate;

import java.util.List;
import java.util.Optional;

public interface AffiliateRepository {

    List<Affiliate> listAll();
    Optional<Affiliate> findByIdOptional(Long id);
    void persist(Affiliate affiliate);
    void delete(Affiliate affiliate);

    Optional<Affiliate> findByDni(String dni);
    Optional<Affiliate> findByHealthInsuranceCode(String healthInsuranceCode);
}