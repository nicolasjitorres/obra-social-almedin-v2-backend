package com.almedin.modules.scheduling.application.dto;

import com.almedin.modules.shared.domain.enums.AppointmentType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record DeriveAppointmentRequest(
        @NotNull Long affiliateId,
        @NotNull Long specialistId,
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        @NotNull AppointmentType type
) {}