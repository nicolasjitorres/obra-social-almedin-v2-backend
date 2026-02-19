package com.almedin.modules.scheduling.application.dto;

public record CompleteAppointmentRequest(
        String clinicalNotes,
        String prescription
) {}