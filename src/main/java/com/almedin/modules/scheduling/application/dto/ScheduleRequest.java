package com.almedin.modules.scheduling.application.dto;

import com.almedin.modules.shared.domain.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ScheduleRequest(

        @NotNull(message = "El especialista es obligatorio")
        Long specialistId,

        @NotNull(message = "El día de la semana es obligatorio")
        DayOfWeek dayOfWeek,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime endTime,

        @NotNull(message = "La duración del slot es obligatoria")
        Integer slotDuration
) {}