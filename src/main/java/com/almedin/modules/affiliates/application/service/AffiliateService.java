package com.almedin.modules.affiliates.application.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.almedin.modules.affiliates.application.dto.AffiliateRequest;
import com.almedin.modules.affiliates.application.dto.AffiliateResponse;
import com.almedin.modules.affiliates.application.dto.UpdateAffiliateProfileRequest;
import com.almedin.modules.affiliates.application.mapper.AffiliateMapper;
import com.almedin.modules.affiliates.domain.exceptions.AffiliateNotFoundException;
import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import com.almedin.modules.shared.application.dto.ChangePasswordRequest;
import com.almedin.modules.shared.application.dto.PageRequest;
import com.almedin.modules.shared.application.dto.PageResponse;
import com.almedin.modules.shared.application.security.SecurityContext;
import com.almedin.modules.shared.domain.enums.Role;
import com.almedin.modules.specialists.domain.model.Specialist;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

import java.util.List;

@ApplicationScoped
public class AffiliateService {

    @Inject
    AffiliateRepository affiliateRepository;

    @Inject
    AffiliateMapper affiliateMapper;

    @Inject
    SecurityContext securityContext;

    public PageResponse<AffiliateResponse> findAll(PageRequest pageRequest, boolean includeInactive) {
        List<AffiliateResponse> content = affiliateMapper.toResponseList(
                affiliateRepository.listAll(pageRequest.page(), pageRequest.size(), includeInactive)
        );
        long total = affiliateRepository.countAll(includeInactive);
        return PageResponse.of(content, pageRequest, total);
    }

    public AffiliateResponse findById(Long id) {
        Affiliate affiliate = getOrThrow(id);
        securityContext.requireSelfOrAdmin(id);
        return affiliateMapper.toResponse(affiliate);
    }

    @Transactional
    public AffiliateResponse create(AffiliateRequest request) {
        if (affiliateRepository.findByDni(request.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un afiliado con el DNI: " + request.dni());
        }
        if (affiliateRepository.findByHealthInsuranceCode(request.healthInsuranceCode()).isPresent()) {
            throw new IllegalArgumentException("El código de obra social ya está registrado");
        }

        Affiliate affiliate = affiliateMapper.toEntity(request);
        affiliate.setRole(Role.AFFILIATE);
        affiliate.setPassword(BCrypt.withDefaults().hashToString(12, request.password().toCharArray()));
        affiliateRepository.persist(affiliate);
        return affiliateMapper.toResponse(affiliate);
    }

    @Transactional
    public AffiliateResponse update(Long id, AffiliateRequest request) {
        Affiliate existing = getOrThrow(id);

        if (!existing.getDni().equals(request.dni()) &&
                affiliateRepository.findByDni(request.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un afiliado con el DNI: " + request.dni());
        }

        affiliateMapper.updateEntityFromRequest(request, existing);
        return affiliateMapper.toResponse(existing);
    }

    @Transactional
    public void deactivate(Long id) {
        affiliateRepository.deactivate(getOrThrow(id));
    }

    private Affiliate getOrThrow(Long id) {
        return affiliateRepository.findById(id)
                .orElseThrow(() -> new AffiliateNotFoundException(id));
    }

    @Transactional
    public AffiliateResponse updateProfile(Long id, UpdateAffiliateProfileRequest request) {
        Affiliate existing = getOrThrow(id);
        affiliateMapper.updateSelfEntityFromRequest(request, existing);
        return affiliateMapper.toResponse(existing);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        Affiliate existing = getOrThrow(id);
        if (!BCrypt.verifyer().verify(request.currentPassword().toCharArray(), existing.getPassword()).verified) {
            throw new ConstraintViolationException("Contraseña actual incorrecta", null);
        }
        existing.setPassword(BCrypt.withDefaults().hashToString(12, request.newPassword().toCharArray()));
        affiliateRepository.update(existing);
    }
}