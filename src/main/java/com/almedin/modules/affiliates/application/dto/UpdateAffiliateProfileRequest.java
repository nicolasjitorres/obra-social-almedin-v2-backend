package com.almedin.modules.affiliates.application.dto;

import jakarta.validation.constraints.*;

public record UpdateAffiliateProfileRequest(
        @NotBlank @Size(min = 2) String firstName,
        @NotBlank @Size(min = 2) String lastName,
        @NotBlank @Email         String email
) {}