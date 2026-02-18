package com.almedin.modules.scheduling.infrastructure.web;

import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.application.service.AppointmentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;

@Path("/api/appointments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Citas", description = "Gestión de citas médicas")
public class AppointmentResource {

    @Inject
    AppointmentService appointmentService;

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener cita por ID")
    public Response getById(@PathParam("id") Long id) {
        try {
            return Response.ok(appointmentService.findById(id)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @GET
    @Path("/affiliate/{affiliateId}")
    @Operation(summary = "Obtener citas de un afiliado")
    public Response getByAffiliate(@PathParam("affiliateId") Long affiliateId) {
        return Response.ok(appointmentService.findByAffiliate(affiliateId)).build();
    }

    @GET
    @Path("/specialist/{specialistId}")
    @Operation(summary = "Obtener citas de un especialista")
    public Response getBySpecialist(@PathParam("specialistId") Long specialistId) {
        return Response.ok(appointmentService.findBySpecialist(specialistId)).build();
    }

    @GET
    @Path("/specialist/{specialistId}/agenda")
    @Operation(summary = "Obtener agenda diaria de un especialista")
    public Response getAgenda(
            @PathParam("specialistId") Long specialistId,
            @QueryParam("date") String date) {
        LocalDate localDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return Response.ok(appointmentService.findBySpecialistAndDate(specialistId, localDate)).build();
    }

    @POST
    @Operation(summary = "Crear una cita")
    public Response create(@Valid AppointmentRequest request) {
        try {
            return Response.status(Response.Status.CREATED)
                    .entity(appointmentService.create(request)).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @PATCH
    @Path("/{id}/cancel")
    @Operation(summary = "Cancelar una cita")
    public Response cancel(@PathParam("id") Long id, @Valid CancelAppointmentRequest request) {
        try {
            return Response.ok(appointmentService.cancel(id, request)).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @PATCH
    @Path("/{id}/complete")
    @Operation(summary = "Completar una cita")
    public Response complete(@PathParam("id") Long id, CompleteAppointmentRequest request) {
        try {
            return Response.ok(appointmentService.complete(id, request)).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @PATCH
    @Path("/{id}/absent")
    @Operation(summary = "Marcar afiliado como ausente")
    public Response markAsAbsent(@PathParam("id") Long id) {
        try {
            return Response.ok(appointmentService.markAsAbsent(id)).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    record ErrorResponse(String message) {}
}