package com.almedin.modules.specialists.domain.model;

import com.almedin.modules.shared.domain.model.User;
import com.almedin.modules.shared.domain.enums.Speciality;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "specialists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Specialist extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La especialidad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Speciality speciality;

    @Column(name = "address")
    private String address;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean active = true;

    public void deactivate() {
        this.active = false;
    }
}