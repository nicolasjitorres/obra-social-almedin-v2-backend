package com.almedin.modules.specialists.application.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.almedin.modules.shared.application.dto.PageRequest;
import com.almedin.modules.shared.application.dto.PageResponse;
import com.almedin.modules.shared.application.security.SecurityContext;
import com.almedin.modules.specialists.application.dto.SpecialistRequest;
import com.almedin.modules.specialists.application.dto.SpecialistResponse;
import com.almedin.modules.specialists.application.mapper.SpecialistMapper;
import com.almedin.modules.specialists.domain.exceptions.SpecialistNotFoundException;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import com.almedin.modules.shared.domain.enums.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SpecialistService {

    @Inject
    SpecialistRepository specialistRepository;

    @Inject
    SpecialistMapper specialistMapper;

    @Inject
    SecurityContext securityContext;

    public SpecialistResponse findById(Long id) {
        Specialist specialist = getOrThrow(id);
        securityContext.requireSelfOrAdmin(id);
        return specialistMapper.toResponse(specialist);
    }

    @Transactional
    public SpecialistResponse create(SpecialistRequest request) {
        if (specialistRepository.findByDni(request.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un especialista con el DNI: " + request.dni());
        }

        Specialist specialist = specialistMapper.toEntity(request);
        specialist.setRole(Role.SPECIALIST);
        specialist.setPassword(BCrypt.withDefaults().hashToString(12, request.password().toCharArray()));
        specialistRepository.persist(specialist);
        return specialistMapper.toResponse(specialist);
    }

    @Transactional
    public SpecialistResponse update(Long id, SpecialistRequest request) {
        Specialist existing = getOrThrow(id);

        if (!existing.getDni().equals(request.dni()) &&
                specialistRepository.findByDni(request.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un especialista con el DNI: " + request.dni());
        }

        specialistMapper.updateEntityFromRequest(request, existing);
        return specialistMapper.toResponse(existing);
    }

    @Transactional
    public void deactivate(Long id) {
        specialistRepository.deactivate(getOrThrow(id));
    }

    private Specialist getOrThrow(Long id) {
        return specialistRepository.findById(id)
                .orElseThrow(() -> new SpecialistNotFoundException(id));
    }

    public PageResponse<SpecialistResponse> findAll(PageRequest pageRequest, String speciality) {
        List<SpecialistResponse> content = specialistMapper.toResponseList(
                specialistRepository.listAll(pageRequest.page(), pageRequest.size(), speciality)
        );
        long total = specialistRepository.countAll(speciality);
        return PageResponse.of(content, pageRequest, total);
    }
}