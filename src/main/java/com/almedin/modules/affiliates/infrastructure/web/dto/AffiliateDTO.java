package com.almedin.modules.affiliates.infrastructure.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AffiliateDTO {
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    private String lastName;

    @Pattern(regexp = "\\d{7,8}", message = "DNI inválido")
    private String dni;

    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "El código de obra social es requerido")
    private String healthInsuranceCode;

    @NotBlank(message = "El rol es requerido")
    private String role;
}