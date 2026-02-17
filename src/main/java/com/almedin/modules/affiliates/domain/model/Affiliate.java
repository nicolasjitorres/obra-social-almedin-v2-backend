package com.almedin.modules.affiliates.domain.model;

import com.almedin.modules.shared.domain.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "affiliates")
@Getter @Setter
public class Affiliate extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de obra social es obligatorio")
    @Column(name = "health_insurance_code", nullable = false)
    private String healthInsuranceCode;


}