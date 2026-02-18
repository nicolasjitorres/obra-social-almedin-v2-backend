package com.almedin.modules.specialists.infrastructure.web;

import com.almedin.modules.specialists.application.dto.SpecialistRequest;
import com.almedin.modules.specialists.application.dto.SpecialistResponse;
import com.almedin.modules.specialists.application.service.SpecialistService;
import com.almedin.modules.specialists.domain.exceptions.SpecialistNotFoundException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/specialists")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Especialistas", description = "Gestión de especialistas")
public class SpecialistResource {

    @Inject
    SpecialistService specialistService;

    @GET
    @Operation(summary = "Obtener todos los especialistas")
    public Response getAll() {
        List<SpecialistResponse> specialists = specialistService.findAll();
        return Response.ok(specialists).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener especialista por ID")
    public Response getById(@PathParam("id") Long id) {
        try {
            return Response.ok(specialistService.findById(id)).build();
        } catch (SpecialistNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @POST
    @Operation(summary = "Crear un especialista")
    public Response create(@Valid SpecialistRequest request) {
        try {
            SpecialistResponse response = specialistService.create(request);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar un especialista")
    public Response update(@PathParam("id") Long id, @Valid SpecialistRequest request) {
        try {
            return Response.ok(specialistService.update(id, request)).build();
        } catch (SpecialistNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Dar de baja un especialista")
    public Response deactivate(@PathParam("id") Long id) {
        try {
            specialistService.deactivate(id);
            return Response.noContent().build();
        } catch (SpecialistNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    record ErrorResponse(String message) {
    }
}
