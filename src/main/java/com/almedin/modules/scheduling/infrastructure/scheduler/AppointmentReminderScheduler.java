package com.almedin.modules.scheduling.infrastructure.scheduler;

import com.almedin.modules.scheduling.domain.model.Appointment;
import com.almedin.modules.scheduling.domain.port.NotificationPort;
import com.almedin.modules.scheduling.domain.repository.AppointmentRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AppointmentReminderScheduler {

    @Inject
    AppointmentRepository appointmentRepository;

    @Inject
    NotificationPort notificationPort;

    @ConfigProperty(name = "almedin.reminder.hours-before", defaultValue = "24")
    int hoursBefore;

    @Transactional
    @Scheduled(cron = "{almedin.reminder.cron}")
    void sendReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(hoursBefore / 24);

        List<Appointment> appointments =
                appointmentRepository.findConfirmedByDateAndReminderNotSent(targetDate);

        for (Appointment appointment : appointments) {
            try {
                notificationPort.sendAppointmentReminder(appointment);
                appointment.setReminderSent(true);
            } catch (Exception e) {
                // Log el error pero continúa con los demás turnos
                System.err.println("Error enviando reminder para turno "
                        + appointment.getId() + ": " + e.getMessage());
            }
        }
    }
}