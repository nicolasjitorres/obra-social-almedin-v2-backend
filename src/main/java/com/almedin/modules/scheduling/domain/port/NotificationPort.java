package com.almedin.modules.scheduling.domain.port;

import com.almedin.modules.scheduling.domain.model.Appointment;

public interface NotificationPort {
    void sendAppointmentReminder(Appointment appointment);
}