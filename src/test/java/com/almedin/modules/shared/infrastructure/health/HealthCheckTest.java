package com.almedin.modules.shared.infrastructure.health;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class HealthCheckTest {

    @Test
    void health_liveness_debeRetornarUp() {
        given()
                .when().get("/q/health/live")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("checks.name", hasItem("Almedin Backend"));
    }

    @Test
    void health_readiness_debeRetornarUp() {
        given()
                .when().get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("checks.name", hasItem("Reminder Scheduler"));
    }

    @Test
    void health_general_debeRetornarUpConTodosLosChecks() {
        given()
                .when().get("/q/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("checks.status", everyItem(equalTo("UP")));
    }
}