package com.almedin.modules.specialists.infrastructure.web;

import com.almedin.modules.shared.application.dto.ChangePasswordRequest;
import com.almedin.modules.shared.application.dto.PageRequest;
import com.almedin.modules.shared.application.dto.PageResponse;
import com.almedin.modules.specialists.application.dto.SpecialistRequest;
import com.almedin.modules.specialists.application.dto.SpecialistResponse;
import com.almedin.modules.specialists.application.dto.UpdateSpecialistProfileRequest;
import com.almedin.modules.specialists.application.service.SpecialistService;
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

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@Path("/api/specialists")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Especialistas", description = "Gestión de especialistas")
public class SpecialistResource {

    @Inject
    SpecialistService specialistService;

    @GET
    @RolesAllowed({"ADMIN", "AFFILIATE"})
    public PageResponse<SpecialistResponse> findAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("speciality") String speciality,
            @QueryParam("includeInactive") @DefaultValue("false") boolean includeInactive) {
        return specialistService.findAll(new PageRequest(page, size), speciality, includeInactive);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "AFFILIATE", "SPECIALIST"})
    @Operation(summary = "Obtener especialista por ID")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(specialistService.findById(id)).build();
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Crear un especialista")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SpecialistRequest.class),
                    examples = @ExampleObject(
                            name = "Ejemplo especialista",
                            summary = "Datos de un especialista válido",
                            value = """
                                    {
                                        "firstName": "Laura",
                                        "lastName": "Gómez",
                                        "dni": "22334455",
                                        "email": "laura.gomez@email.com",
                                        "speciality": "CARDIOLOGIA",
                                        "address": "Av. Corrientes 1234, Buenos Aires"
                                    }
                                    """
                    )
            )
    )
    public Response create(@Valid SpecialistRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(specialistService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Actualizar un especialista")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SpecialistRequest.class),
                    examples = @ExampleObject(
                            name = "Ejemplo actualización",
                            summary = "Datos para actualizar un especialista",
                            value = """
                                    {
                                        "firstName": "Laura Beatriz",
                                        "lastName": "Gómez",
                                        "dni": "22334455",
                                        "email": "laura.gomez@email.com",
                                        "speciality": "NEUROLOGIA",
                                        "address": "Av. Santa Fe 500, Buenos Aires"
                                    }
                                    """
                    )
            )
    )
    public Response update(@PathParam("id") Long id, @Valid SpecialistRequest request) {
        return Response.ok(specialistService.update(id, request)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Dar de baja un especialista")
    public Response deactivate(@PathParam("id") Long id) {
        specialistService.deactivate(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}/profile")
    @RolesAllowed("SPECIALIST")
    @Operation(summary = "Especialista actualiza su propio perfil")
    public Response updateOwnProfile(@PathParam("id") Long id, @Valid UpdateSpecialistProfileRequest request) {
        return Response.ok(specialistService.updateProfile(id, request)).build();
    }

    @PATCH
    @Path("/{id}/password")
    @RolesAllowed({"SPECIALIST", "AFFILIATE", "ADMIN"})
    @Operation(summary = "Cambiar contraseña")
    public Response changePassword(@PathParam("id") Long id, @Valid ChangePasswordRequest request) {
        specialistService.changePassword(id, request);
        return Response.noContent().build();
    }

}