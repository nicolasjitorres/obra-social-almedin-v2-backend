package com.almedin.modules.scheduling.infrastructure.web;

import com.almedin.modules.scheduling.application.dto.AffiliatePenaltyResponse;
import com.almedin.modules.scheduling.domain.model.AffiliatePenalty;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Path("/api/penalties")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Tag(name = "Penalidades", description = "Gestión de penalidades de afiliados")
public class PenaltyResource {

    @Inject
    EntityManager em;

    @GET
    public Response findAll(
            @QueryParam("active") Boolean active,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {

        String condition = active != null ? " WHERE p.active = :active" : "";

        var query = em.createQuery(
                        "SELECT p FROM AffiliatePenalty p" + condition +
                                " ORDER BY p.appliedAt DESC", AffiliatePenalty.class)
                .setFirstResult(page * size)
                .setMaxResults(size);

        if (active != null) query.setParameter("active", active);

        var countQuery = em.createQuery(
                "SELECT COUNT(p) FROM AffiliatePenalty p" + condition);

        if (active != null) countQuery.setParameter("active", active);

        long total = (long) countQuery.getSingleResult();

        List<AffiliatePenaltyResponse> content = query.getResultList().stream()
                .map(p -> new AffiliatePenaltyResponse(
                        p.getId(),
                        p.getAffiliate().getId(),
                        p.getAffiliate().getFirstName() + " " + p.getAffiliate().getLastName(),
                        p.getAffiliate().getDni(),
                        p.getAppointment().getId(),
                        p.getAppliedAt(),
                        p.getSuspendedUntil(),
                        p.getActive()
                ))
                .toList();

        return Response.ok(java.util.Map.of(
                "content", content,
                "totalElements", total,
                "totalPages", (int) Math.ceil((double) total / size),
                "number", page
        )).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deactivate(@PathParam("id") Long id) {
        AffiliatePenalty penalty = em.find(AffiliatePenalty.class, id);
        if (penalty == null) throw new NotFoundException("Penalidad no encontrada");
        penalty.setActive(false);
        em.merge(penalty);
        return Response.noContent().build();
    }
}