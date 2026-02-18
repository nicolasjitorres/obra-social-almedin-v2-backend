package com.almedin.modules.scheduling.infrastructure.web;

import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.application.service.ScheduleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Path("/api/schedules")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Horarios", description = "Gestión de horarios de especialistas")
public class ScheduleResource {

    @Inject
    ScheduleService scheduleService;

    @GET
    @Path("/specialist/{specialistId}")
    @Operation(summary = "Obtener horarios de un especialista")
    public Response getBySpecialist(@PathParam("specialistId") Long specialistId) {
        return Response.ok(scheduleService.findBySpecialist(specialistId)).build();
    }

    @GET
    @Path("/available-slots")
    @Operation(summary = "Obtener slots disponibles de un especialista en una fecha")
    public Response getAvailableSlots(
            @QueryParam("specialistId") Long specialistId,
            @QueryParam("date") String date) {
        List<AvailableSlotResponse> slots = scheduleService.getAvailableSlots(
                specialistId, LocalDate.parse(date));
        return Response.ok(slots).build();
    }

    @POST
    @Operation(summary = "Crear horario para un especialista")
    public Response create(@Valid ScheduleRequest request) {
        try {
            return Response.status(Response.Status.CREATED)
                    .entity(scheduleService.create(request)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar horario")
    public Response update(@PathParam("id") Long id, @Valid ScheduleRequest request) {
        try {
            return Response.ok(scheduleService.update(id, request)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}/deactivate")
    @Operation(summary = "Desactivar horario")
    public Response deactivate(@PathParam("id") Long id) {
        try {
            scheduleService.deactivate(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    record ErrorResponse(String message) {}
}