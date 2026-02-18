package com.almedin.modules.affiliates.application.dto;

import jakarta.validation.constraints.*;

public record AffiliateRequest(

        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres")
        String firstName,

        @NotBlank(message = "El apellido no puede estar vacío")
        @Size(min = 2, message = "El apellido debe tener al menos 2 caracteres")
        String lastName,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener entre 7 y 8 dígitos numéricos")
        String dni,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe proporcionar un email válido")
        String email,

        @NotBlank(message = "El código de obra social es obligatorio")
        String healthInsuranceCode,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}