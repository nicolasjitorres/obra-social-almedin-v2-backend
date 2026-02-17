package com.almedin.modules.scheduling.application.dto;

import com.almedin.modules.shared.domain.enums.AppointmentStatus;
import com.almedin.modules.shared.domain.enums.AppointmentType;
import com.almedin.modules.shared.domain.enums.CancelledBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        Long affiliateId,
        String affiliateName,
        Long specialistId,
        String specialistName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Integer durationMinutes,
        AppointmentType type,
        AppointmentStatus status,
        CancelledBy cancelledBy,
        String cancellationReason,
        String clinicalNotes,
        String prescription,
        Boolean penaltyApplied,
        Boolean reminderSent,
        LocalDateTime createdAt,
        Long parentAppointmentId
) {}