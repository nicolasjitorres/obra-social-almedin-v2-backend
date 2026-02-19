package com.almedin.modules.shared.infrastructure.web;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RateLimitFilterTest {

    @Test
    void login_dentroDelLimite_debeIncluirHeadersRateLimit() {
        given()
                .contentType(ContentType.JSON)
                .header("X-Forwarded-For", "10.0.0.1")
                .body("{\"email\":\"noexiste@test.com\",\"password\":\"wrong\"}")
                .when().post("/api/auth/login")
                .then()
                .header("X-RateLimit-Limit", equalTo("5"))
                .header("X-RateLimit-Remaining", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin@almedin.com", roles = "ADMIN")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "1")})
    void request_general_debeIncluirHeadersRateLimit() {
        given()
                .header("X-Forwarded-For", "10.0.0.2")
                .when().get("/api/specialists")
                .then()
                .statusCode(200)
                .header("X-RateLimit-Limit", equalTo("100"))
                .header("X-RateLimit-Remaining", notNullValue());
    }

    @Test
    void login_superandoLimite_debeRetornar429() {
        // IP única para no interferir con otros tests
        String testIp = "192.168.99.99";

        // 5 requests — dentro del límite
        for (int i = 0; i < 5; i++) {
            given()
                    .contentType(ContentType.JSON)
                    .header("X-Forwarded-For", testIp)
                    .body("{\"email\":\"test@test.com\",\"password\":\"wrong\"}")
                    .when().post("/api/auth/login")
                    .then()
                    .statusCode(not(equalTo(429)));
        }

        // 6to request — debe ser bloqueado
        given()
                .contentType(ContentType.JSON)
                .header("X-Forwarded-For", testIp)
                .body("{\"email\":\"test@test.com\",\"password\":\"wrong\"}")
                .when().post("/api/auth/login")
                .then()
                .statusCode(429)
                .header("Retry-After", notNullValue())
                .body("message", containsString("Demasiadas solicitudes"));
    }
}