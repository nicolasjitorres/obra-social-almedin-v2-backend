package com.almedin.modules.scheduling.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AvailableSlotsRequest(

        @NotNull
        Long specialistId,

        @NotNull
        LocalDate date
) {}