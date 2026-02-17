package com.almedin.modules.scheduling.application.dto;

import com.almedin.modules.shared.domain.enums.AppointmentType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequest(

        @NotNull(message = "El afiliado es obligatorio")
        Long affiliateId,

        @NotNull(message = "El especialista es obligatorio")
        Long specialistId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate date,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,

        @NotNull(message = "El tipo de cita es obligatorio")
        AppointmentType type,

        Long parentAppointmentId
) {}