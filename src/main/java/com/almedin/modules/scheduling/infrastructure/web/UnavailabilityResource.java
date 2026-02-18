package com.almedin.modules.scheduling.infrastructure.web;

import com.almedin.modules.scheduling.application.dto.UnavailabilityRequest;
import com.almedin.modules.scheduling.application.dto.UnavailabilityResponse;
import com.almedin.modules.scheduling.application.service.UnavailabilityService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/unavailability")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "No disponibilidad", description = "Gestión de no disponibilidad de especialistas")
public class UnavailabilityResource {

    @Inject
    UnavailabilityService unavailabilityService;

    @GET
    @Path("/specialist/{specialistId}")
    @Operation(summary = "Obtener períodos de no disponibilidad de un especialista")
    public Response getBySpecialist(@PathParam("specialistId") Long specialistId) {
        List<UnavailabilityResponse> result = unavailabilityService.findBySpecialist(specialistId);
        return Response.ok(result).build();
    }

    @POST
    @Operation(summary = "Registrar no disponibilidad de un especialista")
    public Response create(@Valid UnavailabilityRequest request) {
        try {
            List<UnavailabilityResponse> created = unavailabilityService.create(request);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar registro de no disponibilidad")
    public Response delete(@PathParam("id") Long id) {
        try {
            unavailabilityService.delete(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    record ErrorResponse(String message) {}
}