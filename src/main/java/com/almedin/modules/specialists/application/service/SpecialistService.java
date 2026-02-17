package com.almedin.modules.specialists.application.service;

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

    public List<SpecialistResponse> findAll() {
        return specialistMapper.toResponseList(specialistRepository.listAllSpecialists());
    }

    public SpecialistResponse findById(Long id) {
        return specialistMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public SpecialistResponse create(SpecialistRequest request) {
        if (specialistRepository.findSpecialistByDni(request.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un especialista con el DNI: " + request.dni());
        }

        Specialist specialist = specialistMapper.toEntity(request);
        specialist.setRole(Role.SPECIALIST);
        specialistRepository.persist(specialist);
        return specialistMapper.toResponse(specialist);
    }

    @Transactional
    public SpecialistResponse update(Long id, SpecialistRequest request) {
        Specialist existing = getOrThrow(id);

        if (!existing.getDni().equals(request.dni()) &&
                specialistRepository.findSpecialistByDni(request.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un especialista con el DNI: " + request.dni());
        }

        specialistMapper.updateEntityFromRequest(request, existing);
        return specialistMapper.toResponse(existing);
    }

    @Transactional
    public void delete(Long id) {
        specialistRepository.delete(getOrThrow(id));
    }

    private Specialist getOrThrow(Long id) {
        return specialistRepository.findSpecialistById(id)
                .orElseThrow(() -> new SpecialistNotFoundException(id));
    }
}