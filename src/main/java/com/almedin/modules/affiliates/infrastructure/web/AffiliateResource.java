package com.almedin.modules.affiliates.infrastructure.web;

import com.almedin.modules.affiliates.application.service.AffiliateService;
import com.almedin.modules.affiliates.infrastructure.mappers.AffiliateMapper;
import com.almedin.modules.affiliates.infrastructure.web.dto.AffiliateDTO;
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

    @Inject
    AffiliateMapper affiliateMapper;

    @GET
    @Operation(summary = "Obtener todos los afiliados")
    public List<AffiliateDTO> getAll() {
        return affiliateMapper.toDTOList(affiliateService.findAll());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener un afiliado por ID")
    public AffiliateDTO getById(@PathParam("id") Long id) {
        return affiliateMapper.toDTO(affiliateService.findById(id));
    }

    @POST
    @Operation(summary = "Crear un nuevo afiliado")
    public Response create(@Valid AffiliateDTO dto) {
        var affiliate = affiliateMapper.toEntity(dto);
        var created = affiliateService.create(affiliate);
        return Response.status(Response.Status.CREATED)
                .entity(affiliateMapper.toDTO(created))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar un afiliado existente")
    public AffiliateDTO update(@PathParam("id") Long id, @Valid AffiliateDTO dto) {
        var affiliateData = affiliateMapper.toEntity(dto);
        var updated = affiliateService.update(id, affiliateData);
        return affiliateMapper.toDTO(updated);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar un afiliado")
    public Response delete(@PathParam("id") Long id) {
        affiliateService.delete(id);
        return Response.noContent().build();
    }
}