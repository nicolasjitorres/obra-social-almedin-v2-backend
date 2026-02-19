package com.almedin.modules.scheduling.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record UnavailabilityResponse(
        Long id,
        Long specialistId,
        String specialistName,
        LocalDate dateFrom,
        LocalDate dateTo,
        LocalTime startTime,
        LocalTime endTime,
        String reason
) {}