package com.almedin.modules.scheduling.infrastructure.web;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestSecurity(user = "admin@almedin.com", roles = "ADMIN")
@JwtSecurity(claims = {
        @Claim(key = "userId", value = "1")
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentResourceTest {

    static Long affiliateId;
    static Long specialistId;
    static Long scheduleId;
    static Long appointmentId;


    @Test
    @Order(1)
    void setup_crearAfiliado() {
        String body = """
                {
                    "firstName": "Carlos",
                    "lastName": "Rodríguez",
                    "dni": "77889900",
                    "email": "carlos.turnos@email.com",
                    "healthInsuranceCode": "OSDE-9999",
                    "password": "password123"
                }
                """;

        affiliateId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/affiliates")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    void setup_crearEspecialista() {
        String body = """
                {
                    "firstName": "Marta",
                    "lastName": "López",
                    "dni": "66778899",
                    "email": "marta.turnos@email.com",
                    "speciality": "CARDIOLOGIA",
                    "address": "Av. Corrientes 500",
                    "password": "password123"
                }
                """;

        specialistId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/specialists")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(3)
    void setup_crearHorario() {
        String body = String.format("""
                {
                    "specialistId": %d,
                    "dayOfWeek": "LUNES",
                    "startTime": "09:00:00",
                    "endTime": "13:00:00",
                    "slotDuration": 30
                }
                """, specialistId);

        scheduleId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/schedules")
                .then()
                .statusCode(201)
                .body("dayOfWeek", equalTo("LUNES"))
                .body("slotDuration", equalTo(30))
                .extract().jsonPath().getLong("id");
    }


    @Test
    @Order(4)
    void getAvailableSlots_debeRetornarSlotsDisponibles() {
        given()
                .queryParam("specialistId", specialistId)
                .queryParam("date", "2026-03-09")
                .when().get("/api/schedules/available-slots")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].startTime", notNullValue())
                .body("[0].durationMinutes", equalTo(30));
    }


    @Test
    @Order(5)
    void create_conDatosValidos_debeRetornar201() {
        String body = String.format("""
                {
                    "affiliateId": %d,
                    "specialistId": %d,
                    "date": "2026-03-09",
                    "startTime": "09:00:00",
                    "type": "CONSULTA",
                    "parentAppointmentId": null
                }
                """, affiliateId, specialistId);

        appointmentId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/appointments")
                .then()
                .statusCode(201)
                .body("status", equalTo("CONFIRMADA"))
                .body("type", equalTo("CONSULTA"))
                .body("durationMinutes", equalTo(30))
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(6)
    void create_conSlotOcupado_debeRetornar400() {
        String body = String.format("""
                {
                    "affiliateId": %d,
                    "specialistId": %d,
                    "date": "2026-03-09",
                    "startTime": "09:00:00",
                    "type": "CONSULTA",
                    "parentAppointmentId": null
                }
                """, affiliateId, specialistId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/appointments")
                .then()
                .statusCode(400)
                .body("message", containsString("ya está ocupado"));
    }


    @Test
    @Order(7)
    void getById_debeRetornarCita() {
        given()
                .when().get("/api/appointments/" + appointmentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(appointmentId.intValue()))
                .body("status", equalTo("CONFIRMADA"));
    }

    @Test
    @Order(8)
    void getByAffiliate_debeRetornarCitas() {
        given()
                .when().get("/api/appointments/affiliate/" + affiliateId)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(9)
    void getAgendaEspecialista_debeRetornarCitasDelDia() {
        given()
                .queryParam("date", "2026-03-09")
                .when().get("/api/appointments/specialist/" + specialistId + "/agenda")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }


    @Test
    @Order(10)
    void complete_conNotasYReceta_debeRetornar200() {
        String body = """
                {
                    "clinicalNotes": "Paciente con buena evolución, sin complicaciones",
                    "prescription": "Aspirina 100mg - 1 comprimido diario"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/api/appointments/" + appointmentId + "/complete")
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETADA"))
                .body("clinicalNotes", notNullValue())
                .body("prescription", notNullValue());
    }

    @Test
    @Order(11)
    void complete_citaYaCompletada_debeRetornar409() {
        String body = """
                {
                    "clinicalNotes": "Intento duplicado",
                    "prescription": null
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/api/appointments/" + appointmentId + "/complete")
                .then()
                .statusCode(409);
    }


    @Test
    @Order(12)
    void setup_crearCitaParaCancelar() {
        String body = String.format("""
                {
                    "affiliateId": %d,
                    "specialistId": %d,
                    "date": "2026-03-09",
                    "startTime": "09:30:00",
                    "type": "CONSULTA",
                    "parentAppointmentId": null
                }
                """, affiliateId, specialistId);

        appointmentId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/appointments")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(13)
    void cancel_porEspecialista_debeRetornar200SinMulta() {
        String body = """
                {
                    "cancelledBy": "SPECIALIST",
                    "reason": "El especialista no podrá atender"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/api/appointments/" + appointmentId + "/cancel")
                .then()
                .statusCode(200)
                .body("status", equalTo("CANCELADA"))
                .body("cancelledBy", equalTo("SPECIALIST"))
                .body("penaltyApplied", equalTo(false));
    }

    @Test
    @Order(14)
    void cancel_citaYaCancelada_debeRetornar409() {
        String body = """
                {
                    "cancelledBy": "AFFILIATE",
                    "reason": "Intento duplicado"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/api/appointments/" + appointmentId + "/cancel")
                .then()
                .statusCode(409);
    }


    @Test
    @Order(15)
    void setup_crearCitaParaAusente() {
        String body = String.format("""
                {
                    "affiliateId": %d,
                    "specialistId": %d,
                    "date": "2026-03-09",
                    "startTime": "10:00:00",
                    "type": "CONSULTA",
                    "parentAppointmentId": null
                }
                """, affiliateId, specialistId);

        appointmentId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/appointments")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(16)
    void markAsAbsent_debeAplicarMultaYRetornar200() {
        given()
                .when().patch("/api/appointments/" + appointmentId + "/absent")
                .then()
                .statusCode(200)
                .body("status", equalTo("AUSENTE"))
                .body("penaltyApplied", equalTo(true));
    }


    @Test
    @Order(17)
    void createUnavailability_rangoFechas_debeRetornar201() {
        String body = String.format("""
                {
                    "specialistId": %d,
                    "dateFrom": "2026-03-16",
                    "dateTo": "2026-03-20",
                    "reason": "Vacaciones"
                }
                """, specialistId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/unavailability")
                .then()
                .statusCode(201)
                .body("size()", equalTo(5)); // 5 días
    }

    @Test
    @Order(18)
    void create_enFechaNoDisponible_debeRetornar400() {
        String body = String.format("""
                {
                    "affiliateId": %d,
                    "specialistId": %d,
                    "date": "2026-03-16",
                    "startTime": "09:00:00",
                    "type": "CONSULTA",
                    "parentAppointmentId": null
                }
                """, affiliateId, specialistId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/appointments")
                .then()
                .statusCode(400)
                .body("message", containsString("no está disponible"));
    }


    @Test
    @Order(19)
    void getById_cuandoNoExiste_debeRetornar404() {
        given()
                .when().get("/api/appointments/9999")
                .then()
                .statusCode(404);
    }
}