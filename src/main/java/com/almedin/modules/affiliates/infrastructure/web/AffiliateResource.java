package com.almedin.modules.affiliates.infrastructure.web;

import com.almedin.modules.affiliates.application.dto.AffiliateRequest;
import com.almedin.modules.affiliates.application.dto.AffiliateResponse;
import com.almedin.modules.affiliates.application.service.AffiliateService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
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
    @Operation(summary = "Obtener todos los afiliados")
    public List<AffiliateResponse> getAll() {
        return affiliateService.findAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener un afiliado por ID")
    public AffiliateResponse getById(@PathParam("id") Long id) {
        return affiliateService.findById(id);
    }

    @POST
    @Operation(summary = "Crear un nuevo afiliado")
    public Response create(@Valid AffiliateRequest request) {
        AffiliateResponse created = affiliateService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar un afiliado existente")
    public AffiliateResponse update(@PathParam("id") Long id, @Valid AffiliateRequest request) {
        return affiliateService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar un afiliado")
    public Response delete(@PathParam("id") Long id) {
        affiliateService.delete(id);
        return Response.noContent().build();
    }
}