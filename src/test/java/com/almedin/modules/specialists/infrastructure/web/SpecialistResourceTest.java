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

    @Test
    @Order(7)
    void getById_cuandoExiste_debeRetornar200() {
        // Primero creamos uno para tener un ID real
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Carlos",
                        "lastName": "Mendez",
                        "dni": "11223344",
                        "email": "carlos.mendez@email.com",
                        "speciality": "NEUROLOGIA",
                        "address": "Tucuman 500",
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
                .body("dni", equalTo("11223344"));
    }

    @Test
    @Order(8)
    void update_conDatosValidos_debeRetornar200() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Marta",
                        "lastName": "Lopez",
                        "dni": "99887766",
                        "email": "marta.lopez@email.com",
                        "speciality": "DERMATOLOGIA",
                        "address": "Rivadavia 100",
                        "password": "password123"
                    }
                    """)
                .when().post(BASE_PATH)
                .then().statusCode(201)
                .extract().path("id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Marta Beatriz",
                        "lastName": "Lopez",
                        "dni": "99887766",
                        "email": "marta.lopez@email.com",
                        "speciality": "ONCOLOGIA",
                        "address": "Rivadavia 100",
                        "password": "password123"
                    }
                    """)
                .when().put(BASE_PATH + "/" + id)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Marta Beatriz"))
                .body("speciality", equalTo("ONCOLOGIA"));
    }

    @Test
    @Order(9)
    void delete_cuandoExiste_debeRetornar204() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Juan",
                        "lastName": "Perez",
                        "dni": "77665544",
                        "email": "juan.perez@email.com",
                        "speciality": "UROLOGIA",
                        "address": "Corrientes 800",
                        "password": "password123"
                    }
                    """)
                .when().post(BASE_PATH)
                .then().statusCode(201)
                .extract().path("id");

        given()
                .when().delete(BASE_PATH + "/" + id)
                .then()
                .statusCode(204);

        given()
                .when().get(BASE_PATH + "/" + id)
                .then()
                .statusCode(404);
    }
}