package com.almedin.modules.scheduling.infrastructure.web;

import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.application.service.ScheduleService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
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
    @RolesAllowed({"ADMIN", "AFFILIATE", "SPECIALIST"})
    @Operation(summary = "Obtener horarios de un especialista")
    public Response getBySpecialist(@PathParam("specialistId") Long specialistId) {
        return Response.ok(scheduleService.findBySpecialist(specialistId)).build();
    }

    @GET
    @Path("/available-slots")
    @RolesAllowed({"ADMIN", "AFFILIATE", "SPECIALIST"})
    @Operation(summary = "Obtener slots disponibles de un especialista en una fecha")
    public Response getAvailableSlots(
            @QueryParam("specialistId") Long specialistId,
            @QueryParam("date") String date) {
        List<AvailableSlotResponse> slots = scheduleService.getAvailableSlots(
                specialistId, LocalDate.parse(date));
        return Response.ok(slots).build();
    }

    @POST
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Crear horario para un especialista")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ScheduleRequest.class),
                    examples = @ExampleObject(
                            name = "Ejemplo horario",
                            value = """
                                    {
                                        "specialistId": 1,
                                        "dayOfWeek": "LUNES",
                                        "startTime": "09:00:00",
                                        "endTime": "13:00:00",
                                        "slotDuration": 30
                                    }
                                    """
                    )
            )
    )
    public Response create(@Valid ScheduleRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(scheduleService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Actualizar horario")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ScheduleRequest.class),
                    examples = @ExampleObject(
                            name = "Ejemplo actualización horario",
                            value = """
                                    {
                                        "specialistId": 1,
                                        "dayOfWeek": "LUNES",
                                        "startTime": "10:00:00",
                                        "endTime": "14:00:00",
                                        "slotDuration": 30
                                    }
                                    """
                    )
            )
    )
    public Response update(@PathParam("id") Long id, @Valid ScheduleRequest request) {
        return Response.ok(scheduleService.update(id, request)).build();
    }

    @DELETE
    @Path("/{id}/deactivate")
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Desactivar horario")
    public Response deactivate(@PathParam("id") Long id) {
        scheduleService.deactivate(id);
        return Response.noContent().build();
    }
}