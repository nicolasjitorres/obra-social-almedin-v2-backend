package com.almedin.modules.affiliates.application.dto;

public record AffiliateResponse(
        Long id,
        String firstName,
        String lastName,
        String dni,
        String email,
        String healthInsuranceCode,
        String role,
        Boolean active
) {}