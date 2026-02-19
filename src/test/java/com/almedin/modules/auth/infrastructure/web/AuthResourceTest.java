package com.almedin.modules.auth.infrastructure.web;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.shared.domain.enums.Role;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.shared.domain.enums.Speciality;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthResourceTest {

    @Inject
    EntityManager em;

    private static final String RAW_PASSWORD = "password123";
    private static final String HASHED_PASSWORD =
            BCrypt.withDefaults().hashToString(12, RAW_PASSWORD.toCharArray());

    @BeforeEach
    @Transactional
    void setUp() {
        em.createQuery("DELETE FROM AffiliatePenalty").executeUpdate();
        em.createQuery("DELETE FROM Appointment").executeUpdate();
        em.createQuery("DELETE FROM SpecialistUnavailability").executeUpdate();
        em.createQuery("DELETE FROM Schedule").executeUpdate();
        em.createQuery("DELETE FROM Specialist").executeUpdate();
        em.createQuery("DELETE FROM Affiliate").executeUpdate();

        Affiliate affiliate = new Affiliate();
        affiliate.setFirstName("Juan");
        affiliate.setLastName("Pérez");
        affiliate.setDni("12345678");
        affiliate.setEmail("juan@email.com");
        affiliate.setHealthInsuranceCode("HC001");
        affiliate.setRole(Role.AFFILIATE);
        affiliate.setPassword(HASHED_PASSWORD);
        affiliate.setActive(true);
        em.persist(affiliate);

        Specialist specialist = new Specialist();
        specialist.setFirstName("Laura");
        specialist.setLastName("Gómez");
        specialist.setDni("22334455");
        specialist.setEmail("laura@email.com");
        specialist.setSpeciality(Speciality.CARDIOLOGIA);
        specialist.setRole(Role.SPECIALIST);
        specialist.setPassword(HASHED_PASSWORD);
        specialist.setActive(true);
        em.persist(specialist);
    }

    @Test
    void login_afiliadoValido_debeRetornar200ConToken() {
        given()
                .contentType("application/json")
                .header("X-Forwarded-For", "1.1.1.1")
                .body("""
                {
                    "email": "juan@email.com",
                    "password": "password123"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("token", not(emptyString()))
                .body("role", equalTo("AFFILIATE"))
                .body("fullName", equalTo("Juan Pérez"))
                .body("userId", notNullValue());
    }

    @Test
    void login_especialistaValido_debeRetornar200ConToken() {
        given()
                .contentType("application/json")
                .header("X-Forwarded-For", "1.1.1.2")
                .body("""
                {
                    "email": "laura@email.com",
                    "password": "password123"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("role", equalTo("SPECIALIST"))
                .body("fullName", equalTo("Laura Gómez"));
    }

    @Test
    void login_adminPorDefecto_debeRetornar200ConTokenAdmin() {
        given()
                .contentType("application/json")
                .header("X-Forwarded-For", "1.1.1.3")
                .body("""
                {
                    "email": "admin@almedin.com",
                    "password": "Admin1234!"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("role", equalTo("ADMIN"));
    }

    @Test
    void login_passwordIncorrecto_debeRetornar400() {
        given()
                .contentType("application/json")
                .header("X-Forwarded-For", "1.1.1.4")
                .body("""
                {
                    "email": "juan@email.com",
                    "password": "passwordMal"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("message", containsString("Credenciales inválidas"));
    }

    @Test
    void login_emailInexistente_debeRetornar400() {
        given()
                .contentType("application/json")
                .header("X-Forwarded-For", "1.1.1.1")
                .body("""
                {
                    "email": "noexiste@email.com",
                    "password": "password123"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("message", containsString("Credenciales inválidas"));
    }

    @Test
    void login_emailInvalido_debeRetornar400PorValidacion() {
        given()
                .contentType("application/json")
                .header("X-Forwarded-For", "1.1.1.2")
                .body("""
                {
                    "email": "no-es-un-email",
                    "password": "password123"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin@almedin.com", roles = "ADMIN")
    void login_cuentaDesactivada_debeRetornar409() {
        // Desactivar el afiliado
        given()
                .contentType("application/json")
                .when()
                .delete("/api/affiliates/" + getAffiliateId())
                .then()
                .statusCode(204);

        // Intentar login
        given()
                .contentType("application/json")
                .header("X-Forwarded-For", "1.1.1.3")
                .body("""
                {
                    "email": "juan@email.com",
                    "password": "password123"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(409)
                .body("message", containsString("desactivada"));
    }

    @Test
    void endpointProtegido_sinToken_debeRetornar401() {
        given()
                .when()
                .get("/api/appointments/1")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "juan@email.com", roles = {"AFFILIATE"})
    void endpointProtegido_conTokenValido_debeRetornar200ConListaVacia() {
        given()
                .when()
                .get("/api/schedules/specialist/999999")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "juan@email.com", roles = "AFFILIATE")
    void endpointSoloEspecialista_conRolAfiliado_debeRetornar403() {
        given()
                .when()
                .get("/api/appointments/specialist/1")
                .then()
                .statusCode(403);
    }

    private Long getAffiliateId() {
        return (Long) em.createQuery(
                        "SELECT a.id FROM Affiliate a WHERE a.email = 'juan@email.com'")
                .getSingleResult();
    }

    @Test
    @TestSecurity(user = "afiliado@almedin.com", roles = "AFFILIATE")
    @JwtSecurity(claims = {@Claim(key = "userId", value = "99")})
    void create_cuandoNoEsAdmin_debeRetornar403() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Test",
                        "lastName": "User",
                        "dni": "11111111",
                        "email": "test@email.com",
                        "speciality": "CARDIOLOGIA",
                        "address": "Corrientes 100",
                        "password": "pass123"
                    }
                    """)
                .when().post("/api/specialists")
                .then()
                .statusCode(403);
    }
}