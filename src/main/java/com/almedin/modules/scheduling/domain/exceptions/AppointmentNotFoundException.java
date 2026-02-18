package com.almedin.modules.scheduling.domain.exceptions;

import com.almedin.modules.shared.domain.exceptions.EntityNotFoundException;

public class AppointmentNotFoundException extends EntityNotFoundException {
    public AppointmentNotFoundException(Long id) {
        super("Cita no encontrada con id: " + id);
    }
}