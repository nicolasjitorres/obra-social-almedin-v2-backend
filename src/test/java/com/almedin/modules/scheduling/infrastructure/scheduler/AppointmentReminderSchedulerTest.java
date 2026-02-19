package com.almedin.modules.scheduling.infrastructure.scheduler;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.scheduling.domain.model.Appointment;
import com.almedin.modules.scheduling.domain.port.NotificationPort;
import com.almedin.modules.scheduling.domain.repository.AppointmentRepository;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;
import com.almedin.modules.specialists.domain.model.Specialist;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class AppointmentReminderSchedulerTest {

    @Inject
    AppointmentReminderScheduler scheduler;

    @InjectMock
    AppointmentRepository appointmentRepository;

    @InjectMock
    NotificationPort notificationPort;

    @Test
    void sendReminders_conTurnosPendientes_debeEnviarYMarcarComoEnviado() {
        Appointment appointment = buildAppointment(1L);

        when(appointmentRepository.findConfirmedByDateAndReminderNotSent(any()))
                .thenReturn(List.of(appointment));

        scheduler.sendReminders();

        verify(notificationPort, times(1)).sendAppointmentReminder(appointment);
        assertThat(appointment.getReminderSent()).isTrue();
    }

    @Test
    void sendReminders_sinTurnos_noDebeEnviarNada() {
        when(appointmentRepository.findConfirmedByDateAndReminderNotSent(any()))
                .thenReturn(List.of());

        scheduler.sendReminders();

        verify(notificationPort, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendReminders_conErrorEnUnTurno_debeContinuarConLosRestantes() {
        Appointment failing  = buildAppointment(1L);
        Appointment ok       = buildAppointment(2L);

        when(appointmentRepository.findConfirmedByDateAndReminderNotSent(any()))
                .thenReturn(List.of(failing, ok));

        doThrow(new RuntimeException("SMTP error"))
                .when(notificationPort).sendAppointmentReminder(failing);

        scheduler.sendReminders();

        // El segundo turno si se proceso
        verify(notificationPort, times(1)).sendAppointmentReminder(ok);
        // El que falló no tiene la bandera en true
        assertThat(failing.getReminderSent()).isFalse();
        assertThat(ok.getReminderSent()).isTrue();
    }

    private Appointment buildAppointment(Long id) {
        Affiliate affiliate = new Affiliate();
        affiliate.setEmail("test@email.com");
        affiliate.setFirstName("Test");

        Specialist specialist = new Specialist();
        specialist.setFirstName("Dr");
        specialist.setLastName("Test");

        return Appointment.builder()
                .affiliate(affiliate)
                .specialist(specialist)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.CONFIRMADA)
                .reminderSent(false)
                .build();
    }
}