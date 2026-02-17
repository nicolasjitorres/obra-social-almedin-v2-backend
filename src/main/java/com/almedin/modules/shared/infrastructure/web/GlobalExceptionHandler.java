package com.almedin.modules.shared.infrastructure.web;

import com.almedin.modules.shared.domain.exceptions.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof WebApplicationException webEx) {
            return webEx.getResponse();
        }

        // Entidad no encontrada en base de datos
        if (exception instanceof EntityNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }

        // Violaciobn de reglas de negocio
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", exception.getMessage()))
                    .build();
        }

        // Errores de validacion
        if (exception instanceof ConstraintViolationException cve) {
            List<String> errors = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .toList();

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Error de validación", "errors", errors))
                    .build();
        }

        // Caulquier otro error
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("message", "Error interno del servidor"))
                .build();
    }
}