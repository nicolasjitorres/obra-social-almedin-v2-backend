package com.almedin.modules.affiliates.domain.model;

import com.almedin.modules.shared.domain.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "affiliates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Affiliate extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de obra social es obligatorio")
    @Column(name = "health_insurance_code", nullable = false)
    private String healthInsuranceCode;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean active = true;

    public void deactivate() {
        this.active = false;
    }
}