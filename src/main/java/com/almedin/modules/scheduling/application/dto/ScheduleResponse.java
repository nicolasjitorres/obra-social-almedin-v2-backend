package com.almedin.modules.scheduling.application.dto;

import com.almedin.modules.shared.domain.enums.DayOfWeek;

import java.time.LocalTime;

public record ScheduleResponse(
        Long id,
        Long specialistId,
        String specialistName,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Integer slotDuration,
        Boolean active
) {}