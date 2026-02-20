package com.almedin.modules.auth.infrastructure.web;

import com.almedin.modules.auth.application.dto.AuthRequest;
import com.almedin.modules.auth.application.dto.AuthResponse;
import com.almedin.modules.auth.application.service.AuthService;
import jakarta.annotation.security.PermitAll;
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

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticación", description = "Login y generación de token JWT")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AuthRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Login administrador",
                                    value = """
                        {
                            "email": "admin@almedin.com",
                            "password": "Admin1234!"
                        }
                        """
                            ),
                            @ExampleObject(
                                    name = "Login afiliado",
                                    value = """
                        {
                            "email": "juan.perez@email.com",
                            "password": "password123"
                        }
                        """
                            ),
                            @ExampleObject(
                                    name = "Login especialista",
                                    value = """
                        {
                            "email": "laura.gomez@email.com",
                            "password": "password123"
                        }
                        """
                            )
                    }
            )
    )
    public Response login(@Valid AuthRequest request) {
        AuthResponse response = authService.login(request);
        return Response.ok(response).build();
    }


    @OPTIONS
    @Path("/login")
    @PermitAll
    public Response loginOptions() {
        return Response.ok().build();
    }
}