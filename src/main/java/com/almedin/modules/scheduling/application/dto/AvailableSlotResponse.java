package com.almedin.modules.scheduling.application.dto;

import java.time.LocalTime;

public record AvailableSlotResponse(
        LocalTime startTime,
        LocalTime endTime,
        Integer durationMinutes
) {}