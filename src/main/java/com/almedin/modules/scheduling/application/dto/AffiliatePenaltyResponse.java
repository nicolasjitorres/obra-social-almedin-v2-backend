package com.almedin.modules.scheduling.application.dto;

import java.time.LocalDateTime;

public record AffiliatePenaltyResponse(
        Long id,
        Long affiliateId,
        String affiliateName,
        String affiliateDni,
        Long appointmentId,
        LocalDateTime appliedAt,
        LocalDateTime suspendedUntil,
        Boolean active
) {}