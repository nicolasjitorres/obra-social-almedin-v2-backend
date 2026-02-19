package com.almedin.modules.specialists.domain.exceptions;

import com.almedin.modules.shared.domain.exceptions.EntityNotFoundException;

public class SpecialistNotFoundException extends EntityNotFoundException {
    public SpecialistNotFoundException(Long id) {
        super("Especialista no encontrado con id: " + id);
    }
}