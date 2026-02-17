package com.almedin.modules.affiliates.application.service;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AffiliateService {

    @Inject
    AffiliateRepository affiliateRepository;

    public List<Affiliate> findAll() {
        return affiliateRepository.listAll();
    }

    public Affiliate findById(Long id) {
        return affiliateRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Afiliado no encontrado con ID: " + id));
    }

    @Transactional
    public Affiliate create(Affiliate affiliate) {
        if (affiliateRepository.findByDni(affiliate.getDni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un afiliado con el DNI: " + affiliate.getDni());
        }

        if (affiliateRepository.findByHealthInsuranceCode(affiliate.getHealthInsuranceCode()).isPresent()) {
            throw new IllegalArgumentException("El código de obra social ya está registrado");
        }

        affiliateRepository.persist(affiliate);
        return affiliate;
    }

    @Transactional
    public Affiliate update(Long id, Affiliate data) {
        Affiliate existing = findById(id);

        existing.setFirstName(data.getFirstName());
        existing.setLastName(data.getLastName());
        existing.setDni(data.getDni());
        existing.setHealthInsuranceCode(data.getHealthInsuranceCode());
        existing.setEmail(data.getEmail());

        return existing;
    }

    @Transactional
    public void delete(Long id) {
        Affiliate existing = findById(id);
        affiliateRepository.delete(existing);
    }
}