package com.almedin.modules.scheduling.application.dto;

import com.almedin.modules.shared.domain.enums.CancelledBy;
import jakarta.validation.constraints.NotNull;

public record CancelAppointmentRequest(

        @NotNull(message = "Debe indicar quién cancela")
        CancelledBy cancelledBy,

        String reason
) {}