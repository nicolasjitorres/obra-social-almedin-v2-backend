package com.almedin.modules.specialists.domain.exceptions;

public class SpecialistNotFoundException extends RuntimeException {
    public SpecialistNotFoundException(Long id) {
        super("Especialista no encontrado con id: " + id);
    }
}