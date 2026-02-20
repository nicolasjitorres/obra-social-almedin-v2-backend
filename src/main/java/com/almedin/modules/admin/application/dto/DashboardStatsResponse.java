package com.almedin.modules.admin.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {
    private long totalAffiliates;
    private long activeAffiliates;
    private long totalSpecialists;
    private long activeSpecialists;
    private long appointmentsToday;
    private long appointmentsThisMonth;
    private long pendingAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
}