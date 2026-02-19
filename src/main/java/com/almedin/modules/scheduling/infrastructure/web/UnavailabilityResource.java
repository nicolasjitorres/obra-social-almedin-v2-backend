package com.almedin.modules.scheduling.infrastructure.web;

import com.almedin.modules.scheduling.application.dto.UnavailabilityRequest;
import com.almedin.modules.scheduling.application.dto.UnavailabilityResponse;
import com.almedin.modules.scheduling.application.service.UnavailabilityService;
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

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@Path("/api/unavailability")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "No disponibilidad", description = "Gestión de no disponibilidad de especialistas")
public class UnavailabilityResource {

    @Inject
    UnavailabilityService unavailabilityService;

    @GET
    @Path("/specialist/{specialistId}")
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Obtener períodos de no disponibilidad de un especialista")
    public Response getBySpecialist(@PathParam("specialistId") Long specialistId) {
        return Response.ok(unavailabilityService.findBySpecialist(specialistId)).build();
    }

    @POST
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Registrar no disponibilidad de un especialista")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UnavailabilityRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Día puntual",
                                    value = """
                                            {
                                                "specialistId": 1,
                                                "dateFrom": "2026-03-15",
                                                "dateTo": null,
                                                "startTime": null,
                                                "endTime": null,
                                                "reason": "Capacitación médica"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Rango de fechas",
                                    value = """
                                            {
                                                "specialistId": 1,
                                                "dateFrom": "2026-07-01",
                                                "dateTo": "2026-07-15",
                                                "startTime": null,
                                                "endTime": null,
                                                "reason": "Vacaciones"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Horario parcial",
                                    value = """
                                            {
                                                "specialistId": 1,
                                                "dateFrom": "2026-03-20",
                                                "dateTo": null,
                                                "startTime": "09:00:00",
                                                "endTime": "11:00:00",
                                                "reason": "Consulta externa"
                                            }
                                            """
                            )
                    }
            )
    )
    public Response create(@Valid UnavailabilityRequest request) {
        List<UnavailabilityResponse> created = unavailabilityService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Eliminar registro de no disponibilidad")
    public Response delete(@PathParam("id") Long id) {
        unavailabilityService.delete(id);
        return Response.noContent().build();
    }
}