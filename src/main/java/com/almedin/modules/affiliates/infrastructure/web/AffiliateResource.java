package com.almedin.modules.affiliates.infrastructure.web;

import com.almedin.modules.affiliates.application.dto.AffiliateRequest;
import com.almedin.modules.affiliates.application.dto.AffiliateResponse;
import com.almedin.modules.affiliates.application.service.AffiliateService;
import com.almedin.modules.shared.application.dto.PageRequest;
import com.almedin.modules.shared.application.dto.PageResponse;
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

@Path("/api/affiliates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Afiliados", description = "Gestión de los afiliados de la obra social")
public class AffiliateResource {

    @Inject
    AffiliateService affiliateService;

    @GET
    @RolesAllowed("ADMIN")
    public PageResponse<AffiliateResponse> findAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return affiliateService.findAll(new PageRequest(page, size));
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "AFFILIATE"})
    @Operation(summary = "Obtener un afiliado por ID")
    public AffiliateResponse getById(@PathParam("id") Long id) {
        return affiliateService.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Crear un nuevo afiliado")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AffiliateRequest.class),
                    examples = @ExampleObject(
                            name = "Ejemplo afiliado",
                            summary = "Datos de un afiliado válido",
                            value = """
                    {
                        "firstName": "Juan",
                        "lastName": "Pérez",
                        "dni": "12345678",
                        "email": "juan.perez@email.com",
                        "healthInsuranceCode": "HC-001"
                    }
                    """
                    )
            )
    )
    public Response create(@Valid AffiliateRequest request) {
        AffiliateResponse created = affiliateService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Actualizar un afiliado existente")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AffiliateRequest.class),
                    examples = @ExampleObject(
                            name = "Ejemplo actualización",
                            summary = "Datos para actualizar un afiliado",
                            value = """
                    {
                        "firstName": "Juan Carlos",
                        "lastName": "Pérez",
                        "dni": "12345678",
                        "email": "juancarlos.perez@email.com",
                        "healthInsuranceCode": "HC-001"
                    }
                    """
                    )
            )
    )
    public AffiliateResponse update(@PathParam("id") Long id, @Valid AffiliateRequest request) {
        return affiliateService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Dar de baja un afiliado")
    public Response deactivate(@PathParam("id") Long id) {
        affiliateService.deactivate(id);
        return Response.noContent().build();
    }
}