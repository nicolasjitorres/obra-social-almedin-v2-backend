package com.almedin.modules.scheduling.infrastructure.notification;

import com.almedin.modules.scheduling.domain.model.Appointment;
import com.almedin.modules.scheduling.domain.port.NotificationPort;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MailNotificationAdapter implements NotificationPort {

    @Inject
    Mailer mailer;

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        String affiliateEmail = appointment.getAffiliate().getEmail();
        String affiliateName  = appointment.getAffiliate().getFirstName();
        String specialistName = appointment.getSpecialist().getFirstName()
                + " " + appointment.getSpecialist().getLastName();
        String speciality     = appointment.getSpecialist().getSpeciality().name();
        String date           = appointment.getDate().toString();
        String time           = appointment.getStartTime().toString();

        mailer.send(
                Mail.withHtml(
                        affiliateEmail,
                        "Recordatorio de turno — Obra Social Almedin",
                        buildHtml(affiliateName, specialistName, speciality, date, time)
                )
        );
    }

    private String buildHtml(String affiliate, String specialist,
                             String speciality, String date, String time) {
        return """
            <html>
              <body style="font-family: Arial, sans-serif; color: #1a1a1a; padding: 24px;">
                <h2 style="color: #0e6fad;">Recordatorio de turno médico</h2>
                <p>Hola <strong>%s</strong>,</p>
                <p>Te recordamos que tenés un turno programado para <strong>mañana</strong>:</p>
                <table style="border-collapse: collapse; margin: 16px 0;">
                  <tr>
                    <td style="padding: 6px 16px 6px 0; color: #555;">Especialista</td>
                    <td style="padding: 6px 0;"><strong>Dr/a. %s</strong></td>
                  </tr>
                  <tr>
                    <td style="padding: 6px 16px 6px 0; color: #555;">Especialidad</td>
                    <td style="padding: 6px 0;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 6px 16px 6px 0; color: #555;">Fecha</td>
                    <td style="padding: 6px 0;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 6px 16px 6px 0; color: #555;">Hora</td>
                    <td style="padding: 6px 0;">%s</td>
                  </tr>
                </table>
                <p style="color: #555; font-size: 13px;">
                  Si necesitás cancelar, hacelo con al menos 2 horas de anticipación 
                  para evitar penalidades.
                </p>
                <p style="margin-top: 32px; color: #aaa; font-size: 12px;">
                  Obra Social Almedin — Sistema de gestión de turnos
                </p>
              </body>
            </html>
            """.formatted(affiliate, specialist, speciality, date, time);
    }
}