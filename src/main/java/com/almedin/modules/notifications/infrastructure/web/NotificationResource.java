package com.almedin.modules.notifications.infrastructure.web;

import com.almedin.modules.notifications.application.service.NotificationService;
import com.almedin.modules.notifications.domain.model.NotificationEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Set;

@Path("/api/notifications")
@Tag(name = "Notificaciones", description = "SSE para notificaciones en tiempo real")
public class NotificationResource {

    @Inject
    NotificationService notificationService;

    @Inject
    JWTParser jwtParser;

    @GET
    @Path("/stream/{specialistId}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @PermitAll
    public Multi<NotificationEvent> stream(
            @PathParam("specialistId") Long specialistId,
            @CookieParam("auth_token") String token
    ) {
        if (token == null || token.isBlank()) {
            throw new NotAuthorizedException("Token requerido");
        }

        try {
            JsonWebToken jwt = jwtParser.parse(token);
            Set<String> groups = jwt.getClaim("groups");
            if (groups == null) groups = Set.of();
            boolean isSpecialist = groups.contains("SPECIALIST");
            boolean isAdmin = groups.contains("ADMIN");

            if (!isSpecialist && !isAdmin) {
                throw new ForbiddenException("Acceso denegado");
            }

            if (isSpecialist) {
                String userIdClaim = jwt.getClaim("userId").toString();
                Long userId = Long.valueOf(userIdClaim);
                if (!userId.equals(specialistId)) {
                    System.out.println("error de id");
                    throw new ForbiddenException("No podés acceder al stream de otro especialista");
                }
            }

        } catch (ForbiddenException | NotAuthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new NotAuthorizedException("Token inválido o expirado");
        }

        return notificationService.streamFor(specialistId);
    }
}