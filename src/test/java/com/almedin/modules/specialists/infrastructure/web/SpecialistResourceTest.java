package com.almedin.modules.specialists.infrastructure.web;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestSecurity(user = "admin@almedin.com", roles = "ADMIN")
@JwtSecurity(claims = {
        @Claim(key = "userId", value = "1")
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpecialistResourceTest {

    private static final String BASE_PATH = "/api/specialists";

    private static final String VALID_BODY = """
            {
                "firstName": "Ana",
                "lastName": "Torres",
                "dni": "44556677",
                "email": "ana@email.com",
                "speciality": "CARDIOLOGIA",
                "address": "Belgrano 200",
                "password": "password123"
            }
            """;

    @Test
    @Order(1)
    void getAll_debeRetornar200() {
        given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @Order(2)
    void create_conDatosValidos_debeRetornar201() {
        given()
                .contentType(ContentType.JSON)
                .body(VALID_BODY)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("dni", equalTo("44556677"))
                .body("speciality", equalTo("CARDIOLOGIA"))
                .body("id", notNullValue());
    }

    @Test
    @Order(3)
    void create_conDniDuplicado_debeRetornar409() {
        given()
                .contentType(ContentType.JSON)
                .body(VALID_BODY)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(409)
                .body("message", containsString("DNI"));
    }

    @Test
    @Order(4)
    void create_conEmailInvalido_debeRetornar400() {
        String bodyInvalido = """
                {
                    "firstName": "Pedro",
                    "lastName": "Ruiz",
                    "dni": "55667788",
                    "email": "no-es-un-email",
                    "speciality": "ORTOPEDIA",
                    "address": "Rivadavia 300",
                    "password": "password123"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(bodyInvalido)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    @Order(5)
    void getById_cuandoNoExiste_debeRetornar404() {
        given()
                .when().get(BASE_PATH + "/9999")
                .then()
                .statusCode(404)
                .body("message", containsString("9999"));
    }

    @Test
    @Order(6)
    void delete_cuandoNoExiste_debeRetornar404() {
        given()
                .when().delete(BASE_PATH + "/9999")
                .then()
                .statusCode(404);
    }
}