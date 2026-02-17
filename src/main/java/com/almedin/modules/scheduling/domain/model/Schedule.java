package com.almedin.modules.scheduling.domain.model;

import com.almedin.modules.shared.domain.enums.DayOfWeek;
import com.almedin.modules.specialists.domain.model.Specialist;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El especialista es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @NotNull(message = "El día de la semana es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull(message = "La duración del slot es obligatoria")
    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration;

    @Column(nullable = false)
    private Boolean active = true;
}