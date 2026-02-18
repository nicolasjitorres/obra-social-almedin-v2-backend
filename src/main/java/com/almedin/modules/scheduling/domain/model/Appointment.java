package com.almedin.modules.scheduling.domain.model;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;
import com.almedin.modules.shared.domain.enums.AppointmentType;
import com.almedin.modules.shared.domain.enums.CancelledBy;
import com.almedin.modules.specialists.domain.model.Specialist;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_id", nullable = false)
    private Affiliate affiliate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private CancelledBy cancelledBy;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(name = "prescription", columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "penalty_applied")
    @Builder.Default
    private Boolean penaltyApplied = false;

    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_appointment_id")
    private Appointment parentAppointment;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = AppointmentStatus.PENDIENTE;
    }
}