package com.almedin.modules.affiliates.infrastructure.persistence;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PanacheAffiliateRepository implements AffiliateRepository, PanacheRepository<Affiliate> {

    @Override
    public Optional<Affiliate> findByDni(String dni) {
        return find("dni", dni).firstResultOptional();
    }

    @Override
    public Optional<Affiliate> findByHealthInsuranceCode(String healthInsuranceCode) {
        return find("healthInsuranceCode", healthInsuranceCode).firstResultOptional();
    }

    @Override
    public List<Affiliate> listAll() {
        return PanacheRepository.super.listAll();
    }

    @Override
    public Optional<Affiliate> findByIdOptional(Long id) {
        return PanacheRepository.super.findByIdOptional(id);
    }

    @Override
    public void persist(Affiliate affiliate) {
        PanacheRepository.super.persist(affiliate);
    }

    @Override
    public void delete(Affiliate affiliate) {
        PanacheRepository.super.delete(affiliate);
    }
}