package com.almedin.modules.scheduling.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record UnavailabilityRequest(

        @NotNull(message = "El especialista es obligatorio")
        Long specialistId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate dateFrom,

        LocalDate dateTo,
        LocalTime startTime,
        LocalTime endTime,
        String reason
) {}