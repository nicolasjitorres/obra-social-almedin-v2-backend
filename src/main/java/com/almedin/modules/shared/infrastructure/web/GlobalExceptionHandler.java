package com.almedin.modules.shared.infrastructure.web;

import com.almedin.modules.shared.domain.exceptions.BusinessRuleViolationException;
import com.almedin.modules.shared.domain.exceptions.EntityNotFoundException;
import io.quarkus.security.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        // Errores HTTP nativos
        if (exception instanceof WebApplicationException webEx) {
            return webEx.getResponse();
        }

        // Entidad no encontrada
        if (exception instanceof EntityNotFoundException e) {
            return build(Response.Status.NOT_FOUND, "Recurso no encontrado", e.getMessage());
        }

        // Violación de regla de negocio específica
        if (exception instanceof BusinessRuleViolationException e) {
            return build(Response.Status.BAD_REQUEST, "Solicitud inválida", e.getMessage());
        }

        // Regla de negocio violada o dato duplicado
        if (exception instanceof IllegalArgumentException e) {
            return build(Response.Status.BAD_REQUEST, "Solicitud inválida", e.getMessage());
        }

        // Estado invalido (cuenta desactivada, etc)
        if (exception instanceof IllegalStateException e) {
            return build(Response.Status.CONFLICT, "Conflicto de estado", e.getMessage());
        }

        // Errores de validacion de bean (@NotBlank, @Email, etc)
        if (exception instanceof ConstraintViolationException e) {
            String errors = e.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return build(Response.Status.BAD_REQUEST, "Error de validación", errors);
        }

        // Error de autenticación o autorizacion
        if (exception instanceof UnauthorizedException e) {
            return build(Response.Status.FORBIDDEN, "Acceso denegado", e.getMessage());
        }

        // Cualquier otro error no contemplado
        return build(Response.Status.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                "Ocurrió un error inesperado. Por favor intente más tarde.");
    }

    private Response build(Response.Status status, String error, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(status.getStatusCode(), error, message))
                .build();
    }

    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {
        public ErrorResponse(int status, String error, String message) {
            this(status, error, message, LocalDateTime.now());
        }
    }
}