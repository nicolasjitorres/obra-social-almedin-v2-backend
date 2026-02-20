package com.almedin.modules.specialists.application.dto;

import jakarta.validation.constraints.*;

public record UpdateSpecialistProfileRequest(
        @NotBlank @Size(min = 2) String firstName,
        @NotBlank @Size(min = 2) String lastName,
        @NotBlank @Email        String email,
        String address
) {}