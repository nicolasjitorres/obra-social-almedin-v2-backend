package com.almedin.modules.scheduling.infrastructure.web;

import com.almedin.modules.scheduling.application.dto.*;
import com.almedin.modules.scheduling.application.service.AppointmentService;
import com.almedin.modules.shared.application.dto.PageRequest;
import com.almedin.modules.shared.application.dto.PageResponse;
import com.almedin.modules.shared.domain.enums.AppointmentStatus;
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

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@Path("/api/appointments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Citas", description = "Gestión de citas médicas")
public class AppointmentResource {

    @Inject
    AppointmentService appointmentService;

    @GET
    @RolesAllowed("ADMIN")
    @Operation(summary = "Listar todas las citas con paginación")
    public PageResponse<AppointmentResponse> findAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("status") AppointmentStatus status) {
        return appointmentService.findAll(new PageRequest(page, size), status);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Obtener cita por ID")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(appointmentService.findById(id)).build();
    }

    @GET
    @Path("/affiliate/{affiliateId}")
    @RolesAllowed({"ADMIN", "AFFILIATE"})
    @Operation(summary = "Obtener citas de un afiliado")
    public PageResponse<AppointmentResponse> getByAffiliate(
            @PathParam("affiliateId") Long affiliateId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return appointmentService.findByAffiliate(affiliateId, new PageRequest(page, size));
    }

    @GET
    @Path("/specialist/{specialistId}")
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Obtener citas de un especialista")
    public PageResponse<AppointmentResponse> getBySpecialist(
            @PathParam("specialistId") Long specialistId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return appointmentService.findBySpecialist(specialistId, new PageRequest(page, size));
    }

    @GET
    @Path("/specialist/{specialistId}/agenda")
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Obtener agenda diaria de un especialista")
    public Response getAgenda(
            @PathParam("specialistId") Long specialistId,
            @QueryParam("date") String date) {
        LocalDate localDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return Response.ok(appointmentService.findBySpecialistAndDate(specialistId, localDate)).build();
    }

    @POST
    @RolesAllowed({"ADMIN", "AFFILIATE"})
    @Operation(summary = "Crear una cita")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AppointmentRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Consulta estándar",
                                    summary = "Cita de consulta médica",
                                    value = """
                                            {
                                                "affiliateId": 1,
                                                "specialistId": 1,
                                                "date": "2026-03-09",
                                                "startTime": "09:00:00",
                                                "type": "CONSULTA",
                                                "parentAppointmentId": null
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Cita derivada",
                                    summary = "Cita derivada de una consulta previa",
                                    value = """
                                            {
                                                "affiliateId": 1,
                                                "specialistId": 1,
                                                "date": "2026-03-16",
                                                "startTime": "10:00:00",
                                                "type": "EXTRACCION",
                                                "parentAppointmentId": 1
                                            }
                                            """
                            )
                    }
            )
    )
    public Response create(@Valid AppointmentRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(appointmentService.create(request)).build();
    }

    @PATCH
    @Path("/{id}/cancel")
    @RolesAllowed({"ADMIN", "AFFILIATE", "SPECIALIST"})
    @Operation(summary = "Cancelar una cita")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CancelAppointmentRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Cancelación por afiliado",
                                    value = """
                                            {
                                                "cancelledBy": "AFFILIATE",
                                                "reason": "No puedo asistir por motivos laborales"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Cancelación por especialista",
                                    value = """
                                            {
                                                "cancelledBy": "SPECIALIST",
                                                "reason": "Emergencia médica del profesional"
                                            }
                                            """
                            )
                    }
            )
    )
    public Response cancel(@PathParam("id") Long id, @Valid CancelAppointmentRequest request) {
        return Response.ok(appointmentService.cancel(id, request)).build();
    }

    @PATCH
    @Path("/{id}/complete")
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Completar una cita")
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CompleteAppointmentRequest.class),
                    examples = @ExampleObject(
                            name = "Completar con notas",
                            value = """
                                    {
                                        "clinicalNotes": "Paciente presenta buena evolución. Tensión arterial normal.",
                                        "prescription": "Enalapril 10mg - 1 comprimido diario en ayunas por 90 días."
                                    }
                                    """
                    )
            )
    )
    public Response complete(@PathParam("id") Long id, CompleteAppointmentRequest request) {
        return Response.ok(appointmentService.complete(id, request)).build();
    }

    @PATCH
    @Path("/{id}/absent")
    @RolesAllowed({"ADMIN", "SPECIALIST"})
    @Operation(summary = "Marcar afiliado como ausente")
    public Response markAsAbsent(@PathParam("id") Long id) {
        return Response.ok(appointmentService.markAsAbsent(id)).build();
    }
}