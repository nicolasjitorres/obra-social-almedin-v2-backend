package com.almedin.modules.admin.infrastructure.web;

import com.almedin.modules.admin.application.dto.DashboardStatsResponse;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.ZoneId;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Tag(name = "Admin")
public class AdminResource {

    @Inject
    EntityManager em;

    @GET
    @Path("/dashboard")
    public Response getDashboardStats() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate lastOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        long totalAffiliates = (long) em.createQuery(
                "SELECT COUNT(a) FROM Affiliate a").getSingleResult();

        long activeAffiliates = (long) em.createQuery(
                "SELECT COUNT(a) FROM Affiliate a WHERE a.active = true").getSingleResult();

        long totalSpecialists = (long) em.createQuery(
                "SELECT COUNT(s) FROM Specialist s").getSingleResult();

        long activeSpecialists = (long) em.createQuery(
                "SELECT COUNT(s) FROM Specialist s WHERE s.active = true").getSingleResult();

        long appointmentsToday = (long) em.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.date = :today")
                .setParameter("today", today).getSingleResult();

        long appointmentsThisMonth = (long) em.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.date >= :from AND a.date <= :to")
                .setParameter("from", firstOfMonth)
                .setParameter("to", lastOfMonth).getSingleResult();

        long pendingAppointments = (long) em.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
                .setParameter("status", AppointmentStatus.CONFIRMADA)
                .getSingleResult();

        long completedAppointments = (long) em.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
                .setParameter("status", AppointmentStatus.COMPLETADA)
                .getSingleResult();

        long cancelledAppointments = (long) em.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
                .setParameter("status", AppointmentStatus.CANCELADA)
                .getSingleResult();

        return Response.ok(DashboardStatsResponse.builder()
                .totalAffiliates(totalAffiliates)
                .activeAffiliates(activeAffiliates)
                .totalSpecialists(totalSpecialists)
                .activeSpecialists(activeSpecialists)
                .appointmentsToday(appointmentsToday)
                .appointmentsThisMonth(appointmentsThisMonth)
                .pendingAppointments(pendingAppointments)
                .completedAppointments(completedAppointments)
                .cancelledAppointments(cancelledAppointments)
                .build()).build();
    }
}