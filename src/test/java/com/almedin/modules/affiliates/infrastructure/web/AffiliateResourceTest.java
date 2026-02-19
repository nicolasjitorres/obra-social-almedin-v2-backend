package com.almedin.modules.affiliates.infrastructure.web;

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
class AffiliateResourceTest {

    private static final String BASE_PATH = "/api/affiliates";

    private static final String VALID_BODY = """
            {
                "firstName": "Carlos",
                "lastName": "López",
                "dni": "11223344",
                "email": "carlos@email.com",
                "healthInsuranceCode": "OS-003",
                "password": "password123"
            }
            """;

    @Test
    @Order(1)
    void getAll_debeRetornarPaginaConMetadata() {
        given()
                .when().get(BASE_PATH + "?page=0&size=10")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("content", notNullValue())
                .body("page", equalTo(0))
                .body("size", equalTo(10))
                .body("totalElements", notNullValue())
                .body("totalPages", notNullValue())
                .body("first", equalTo(true));
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
                .body("dni", equalTo("11223344"))
                .body("email", equalTo("carlos@email.com"))
                .body("id", notNullValue());
    }

    @Test
    @Order(3)
    void create_conDniDuplicado_debeRetornar400() {
        given()
                .contentType(ContentType.JSON)
                .body(VALID_BODY)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("message", containsString("DNI"));
    }

    @Test
    @Order(4)
    void create_conEmailInvalido_debeRetornar400() {
        String bodyInvalido = """
                {
                    "firstName": "Ana",
                    "lastName": "Ruiz",
                    "dni": "55667788",
                    "email": "no-es-un-email",
                    "healthInsuranceCode": "OS-004",
                    "password": "password123"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(bodyInvalido)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("errors", not(empty()));
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

    @Test
    @Order(7)
    void getById_cuandoExiste_debeRetornar200() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Marta",
                        "lastName": "Gomez",
                        "dni": "99887766",
                        "email": "marta@email.com",
                        "healthInsuranceCode": "OS-005",
                        "password": "password123"
                    }
                    """)
                .when().post(BASE_PATH)
                .then().statusCode(201)
                .extract().path("id");

        given()
                .when().get(BASE_PATH + "/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("dni", equalTo("99887766"));
    }

    @Test
    @Order(8)
    void create_conNombreVacio_debeRetornar400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "",
                        "lastName": "Ramirez",
                        "dni": "33445566",
                        "email": "ramirez@email.com",
                        "healthInsuranceCode": "OS-006",
                        "password": "password123"
                    }
                    """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("errors", not(empty()));
    }
}