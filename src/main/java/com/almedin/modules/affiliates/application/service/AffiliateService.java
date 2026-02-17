package com.almedin.modules.affiliates.application.service;

import com.almedin.modules.affiliates.application.dto.AffiliateRequest;
import com.almedin.modules.affiliates.application.dto.AffiliateResponse;
import com.almedin.modules.affiliates.application.mapper.AffiliateMapper;
import com.almedin.modules.affiliates.domain.exceptions.AffiliateNotFoundException;
import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import com.almedin.modules.shared.domain.enums.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class AffiliateService {

    @Inject
    AffiliateRepository affiliateRepository;

    @Inject
    AffiliateMapper affiliateMapper;

    public List<AffiliateResponse> findAll() {
        return affiliateMapper.toResponseList(affiliateRepository.listAllAffiliates());
    }

    public AffiliateResponse findById(Long id) {
        return affiliateMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public AffiliateResponse create(AffiliateRequest request) {
        if (affiliateRepository.findAffiliateByDni(request.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un afiliado con el DNI: " + request.dni());
        }
        if (affiliateRepository.findAffiliateByHealthInsuranceCode(request.healthInsuranceCode()).isPresent()) {
            throw new IllegalArgumentException("El código de obra social ya está registrado");
        }

        Affiliate affiliate = affiliateMapper.toEntity(request);
        affiliate.setRole(Role.AFFILIATE);
        affiliateRepository.persist(affiliate);
        return affiliateMapper.toResponse(affiliate);
    }

    @Transactional
    public AffiliateResponse update(Long id, AffiliateRequest request) {
        Affiliate existing = getOrThrow(id);

        if (!existing.getDni().equals(request.dni()) &&
                affiliateRepository.findAffiliateByDni(request.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un afiliado con el DNI: " + request.dni());
        }

        affiliateMapper.updateEntityFromRequest(request, existing);
        return affiliateMapper.toResponse(existing);
    }

    @Transactional
    public void delete(Long id) {
        affiliateRepository.delete(getOrThrow(id));
    }

    private Affiliate getOrThrow(Long id) {
        return affiliateRepository.findAffiliateById(id)
                .orElseThrow(() -> new AffiliateNotFoundException(id));
    }
}