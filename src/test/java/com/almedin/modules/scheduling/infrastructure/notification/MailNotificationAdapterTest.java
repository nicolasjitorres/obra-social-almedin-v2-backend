package com.almedin.modules.scheduling.infrastructure.notification;

import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.scheduling.domain.model.Appointment;
import com.almedin.modules.shared.domain.enums.Speciality;
import com.almedin.modules.specialists.domain.model.Specialist;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class MailNotificationAdapterTest {

    @Inject
    MailNotificationAdapter adapter;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void setUp() {
        mailbox.clear();
    }

    @Test
    void sendAppointmentReminder_debeEnviarEmailConDatosCorrectos() {
        Affiliate affiliate = new Affiliate();
        affiliate.setEmail("juan@email.com");
        affiliate.setFirstName("Juan");

        Specialist specialist = new Specialist();
        specialist.setFirstName("María");
        specialist.setLastName("García");
        specialist.setSpeciality(Speciality.CARDIOLOGIA);

        Appointment appointment = Appointment.builder()
                .affiliate(affiliate)
                .specialist(specialist)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .build();

        adapter.sendAppointmentReminder(appointment);

        var sent = mailbox.getMailMessagesSentTo("juan@email.com");
        assertThat(sent).hasSize(1);
        assertThat(sent.get(0).getSubject()).contains("Recordatorio de turno");
        assertThat(sent.get(0).getHtml()).contains("Juan");
        assertThat(sent.get(0).getHtml()).contains("María García");
        assertThat(sent.get(0).getHtml()).contains("CARDIOLOGIA");
    }
}