package com.almedin.modules.shared.infrastructure.web;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        // Error de negocio
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", exception.getMessage()))
                    .build();
        }

        // Error de recurso no encontrado
        if (exception.getMessage() != null && exception.getMessage().contains("no encontrado")) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", exception.getMessage()))
                    .build();
        }

        // Error inesperado
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno del servidor", "details", exception.getMessage()))
                .build();
    }
}