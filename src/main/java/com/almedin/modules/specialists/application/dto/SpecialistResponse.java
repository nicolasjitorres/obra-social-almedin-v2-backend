package com.almedin.modules.specialists.application.dto;

import com.almedin.modules.shared.domain.enums.Speciality;

public record SpecialistResponse(
        Long id,
        String firstName,
        String lastName,
        String dni,
        String email,
        Speciality speciality,
        String address,
        String role,
        Boolean active
) {}