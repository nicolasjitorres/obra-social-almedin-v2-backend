# 🏥 Obra Social Almedin — Backend v2

Backend REST de un sistema de gestión de obra social médica, desarrollado con **Quarkus 3** siguiendo **arquitectura hexagonal**. Permite la gestión de afiliados, especialistas, turnos, horarios y autenticación JWT con roles.

![CI](https://github.com/nicolasjitorres/obra-social-almedin-v2-backend/actions/workflows/ci.yml/badge.svg)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.oracle.com/java/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.31.3-blue?logo=quarkus)](https://quarkus.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Tests](https://img.shields.io/badge/Tests-94%20passing-brightgreen)](https://github.com/nicolasjitorres/obra-social-almedin-v2-backend)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Tech Stack](#-tech-stack)
- [Arquitectura](#-arquitectura)
- [Diagrama de Módulos](#-diagrama-de-módulos)
- [Diagrama de Carpetas](#-diagrama-de-carpetas)
- [Diagrama de Clases](#-diagrama-de-clases)
- [DER — Diagrama Entidad-Relación](#-der--diagrama-entidad-relación)
- [Casos de Uso](#-casos-de-uso)
- [Endpoints](#-endpoints)
- [Autenticación JWT](#-autenticación-jwt)
- [Seguridad](#-seguridad)
- [Cómo correr el proyecto](#-cómo-correr-el-proyecto)
- [Tests](#-tests)
- [Decisiones Técnicas](#-decisiones-técnicas)
- [Roadmap](#-roadmap)

---

## 📌 Descripción

Sistema backend para una obra social médica que gestiona:

- **Afiliados**: alta, baja lógica, consulta y modificación
- **Especialistas**: gestión de profesionales médicos con especialidades
- **Turnos**: reserva, cancelación, confirmación y seguimiento de citas médicas
- **Horarios**: configuración de disponibilidad semanal de especialistas
- **Penalidades**: suspensión automática de afiliados por ausencias reiteradas
- **Autenticación**: JWT con roles diferenciados (AFFILIATE, SPECIALIST, ADMIN)

---

## 💡 Motivación

Este proyecto surge como práctica de arquitectura backend moderna aplicada a un dominio realista: la gestión de una obra social médica. El objetivo fue ir más allá del clásico CRUD con auth y construir un sistema con **reglas de negocio reales**, **seguridad por roles**, **procesamiento en segundo plano** y una **suite de tests integral**. Elegí este dominio porque presenta complejidad genuina: turnos con validaciones de solapamiento, penalidades automáticas por ausencia, soft delete en entidades, disponibilidad configurable por especialista, y derivación de consultas.

El proyecto también fue una oportunidad para aprender y aplicar **arquitectura hexagonal** en un contexto real, separando dominio de infraestructura de forma que el código de negocio no dependa de Quarkus, Hibernate ni ningún framework.

---

## 💻 Tech Stack

| Categoría | Tecnología |
|-----------|-----------|
| Lenguaje | Java 17 |
| Framework | Quarkus 3.31.3 |
| ORM | Hibernate ORM |
| Base de datos | PostgreSQL 16 |
| Autenticación | SmallRye JWT (RS256) |
| Hash de contraseñas | BCrypt (at.favre.lib) |
| Validación | Hibernate Validator |
| Documentación API | SmallRye OpenAPI + Swagger UI |
| Mapeo de objetos | MapStruct |
| Tests | JUnit 5 + Mockito + REST Assured |
| Contenedores de test | Testcontainers |
| Cobertura | JaCoCo |
| Build | Maven 3.9 |

---

## 🏛️ Arquitectura

El proyecto implementa **arquitectura hexagonal** , separando claramente el dominio de la infraestructura.

![Diagrama de Arquitectura](./images/architecture_diagram.png)


### Principios aplicados

- **Inversión de dependencias**: los servicios dependen de interfaces (puertos), no de implementaciones concretas
- **Separación de responsabilidades**: dominio agnóstico de frameworks
- **Baja de lógica**: toda validación de negocio vive en el dominio o en los servicios de aplicación
- **GlobalExceptionMapper**: manejo centralizado de errores sin try/catch en los resources

---

## 🗂 Diagrama de Clases

![Diagrama de Clases](./images/class_diagram.png)

---

## 🗄 DER — Diagrama Entidad-Relación

![Diagrama Entidad Relacion](./images/entity_relationship_diagram.png)

---

## 🎯 Casos de Uso

![Diagrama de Casos de Uso](./images/use_case_diagram.png)

---

## 🔌 Endpoints

### 🔑 Auth

| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| `POST` | `/api/auth/login` | Login y obtención de JWT | Público |


---

### 🧑 Afiliados — `/api/affiliates`

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| `GET` | `/api/affiliates` | Listar todos los afiliados activos | ADMIN |
| `GET` | `/api/affiliates/{id}` | Obtener afiliado por ID | ADMIN, AFFILIATE (propio) |
| `POST` | `/api/affiliates` | Crear afiliado | ADMIN |
| `PUT` | `/api/affiliates/{id}` | Actualizar afiliado | ADMIN, AFFILIATE (propio) |
| `DELETE` | `/api/affiliates/{id}` | Dar de baja (soft delete) | ADMIN |

---

### 🩺 Especialistas — `/api/specialists`

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| `GET` | `/api/specialists` | Listar todos los especialistas activos | ADMIN, AFFILIATE |
| `GET` | `/api/specialists/{id}` | Obtener especialista por ID | ADMIN, AFFILIATE |
| `POST` | `/api/specialists` | Crear especialista | ADMIN |
| `PUT` | `/api/specialists/{id}` | Actualizar especialista | ADMIN, SPECIALIST (propio) |
| `DELETE` | `/api/specialists/{id}` | Dar de baja (soft delete) | ADMIN |

---

### 📅 Turnos — `/api/appointments`

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| `GET` | `/api/appointments` | Listar todos los turnos | ADMIN |
| `GET` | `/api/appointments/{id}` | Obtener turno por ID | ADMIN, AFFILIATE, SPECIALIST |
| `GET` | `/api/appointments/affiliate/{id}` | Turnos de un afiliado | ADMIN, AFFILIATE (propio) |
| `GET` | `/api/appointments/specialist/{id}` | Turnos de un especialista | ADMIN, SPECIALIST (propio) |
| `POST` | `/api/appointments` | Reservar turno | AFFILIATE |
| `PATCH` | `/api/appointments/{id}/cancel` | Cancelar turno | ADMIN, AFFILIATE |
| `PATCH` | `/api/appointments/{id}/complete` | Marcar como completado | SPECIALIST |
| `PATCH` | `/api/appointments/{id}/absent` | Marcar ausencia | SPECIALIST |
| `POST` | `/api/appointments/{id}/reschedule` | Reprogramar turno | SPECIALIST |

---

### 🗓 Horarios — `/api/schedules`

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| `GET` | `/api/schedules/specialist/{id}` | Horarios de un especialista | ADMIN, AFFILIATE |
| `GET` | `/api/schedules/specialist/{id}/slots` | Slots disponibles por fecha | ADMIN, AFFILIATE |
| `POST` | `/api/schedules` | Crear horario | SPECIALIST |
| `PUT` | `/api/schedules/{id}` | Actualizar horario | SPECIALIST |
| `DELETE` | `/api/schedules/{id}` | Desactivar horario | SPECIALIST |
| `POST` | `/api/schedules/specialist/{id}/unavailability` | Registrar indisponibilidad | SPECIALIST |

---

## 🔒 Autenticación JWT

El sistema usa **JWT firmado con RSA (RS256)**. Al hacer login se retorna un token que debe enviarse en cada request protegido:

```
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

### Roles y permisos

| Role | Descripción |
|------|-------------|
| `ADMIN` | Acceso total. Gestión de afiliados, especialistas y turnos |
| `AFFILIATE` | Puede reservar y cancelar sus propios turnos |
| `SPECIALIST` | Puede gestionar sus horarios y completar/marcar ausencias |

### Credenciales por defecto (Admin)

```
Email:    admin@almedin.com
Password: Admin1234!
```

> Las credenciales del admin por defecto son configurables via `application.properties`:
> ```properties
> almedin.admin.email=admin@almedin.com
> almedin.admin.password=Admin1234!
> ```

---

## 🔐 Seguridad

| Aspecto | Implementación |
|--------|---------------|
| Algoritmo JWT | RS256 (clave pública/privada) |
| Hash de contraseñas | BCrypt con cost factor 12 |
| Claves RSA | Par `private.pem` / `public.pem` — **nunca commiteadas en producción, se deben crear usando OpenSSL para pruebas** |
| Secretos en producción | Configurables via variables de entorno (ver sección Variables de entorno) |
| Autorización | Control de Acceso Basado en Roles (RBAC) |
| Errores de auth | 403 genérico para no exponer información sensible |

---

## 🚀 Cómo correr el proyecto

### Prerrequisitos

- Java 17+
- Maven 3.9+
- Docker (para la base de datos en desarrollo/test)

### 1. Clonar el repositorio

```bash
git clone https://github.com/nicolasjitorres/obra-social-almedin-v2-backend.git
cd obra-social-almedin-v2-backend
```

### 2. Modo desarrollo (Dev Services)

Quarkus levanta PostgreSQL automáticamente via Docker:

```bash
./mvnw quarkus:dev
```

La API queda disponible en `http://localhost:8080/api`  
Swagger UI en `http://localhost:8080/q/swagger-ui`

### 3. Modo producción

Configurar la base de datos en `application.properties`:

```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/almedin
quarkus.datasource.username=tu_usuario
quarkus.datasource.password=tu_password
quarkus.hibernate-orm.database.generation=update
```

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### 4. Variables de entorno (producción)

| Variable      | Descripción                        |
|---------------|------------------------------------|
| `DB_URL`      | URL de conexión PostgreSQL         |
| `DB_USER`     | Usuario de la base de datos        |
| `DB_PASSWORD` | Contraseña de la base de datos     |
| MAIL_HOST     | Servidor SMTP (ej: smtp.gmail.com) |
| MAIL_PORT     | Puerto SMTP (ej: 587)              | 
| MAIL_USERNAME | Usuario SMTP | 
| MAIL_PASSWORD | Contraseña SMTP | 
| MAIL_FROM | Dirección remitente |

---

## 📝 Tests

El proyecto cuenta con **94 tests** distribuidos en 3 niveles:

```
Tests run: 94, Failures: 0, Errors: 0, Skipped: 0
```

### Distribución por módulo

| Módulo | Unit Tests | Repository Tests | HTTP Tests | Total |
|--------|-----------|-----------------|------------|-------|
| Affiliates | 8 | 5 | 6 | 19 |
| Specialists | 7 | 5 | 6 | 18 |
| Scheduling | 13 | 9 | 19 | 41 |
| Auth | 6 | — | 10 | 16 |
| **Total** | **34** | **19** | **41** | **94** |

### Ejecutar los tests

```bash
# Todos los tests
./mvnw test

# Con reporte de cobertura JaCoCo
./mvnw verify

# Ver reporte en: target/site/jacoco/index.html
```

---

## 🧠 Decisiones Técnicas

### ¿Por qué Quarkus en lugar de Spring Boot?
Quarkus está diseñado para cloud-native desde el inicio: tiempo de arranque en milisegundos, menor consumo de memoria, y soporte nativo para GraalVM. Para un sistema como este, donde se podría desplegar en contenedores, esas características son relevantes. También fue una oportunidad de aprender un framework que está creciendo fuertemente en el ecosistema enterprise Java.

### ¿Por qué arquitectura hexagonal?
Porque separa el dominio de la infraestructura de forma que el código de negocio no sabe nada de Quarkus, Hibernate, ni REST. Esto permite testear la lógica de negocio en aislamiento (unit tests puros con mocks) sin levantar ningún contexto de framework.

### ¿Por qué JWT con RS256 en lugar de HS256?
Con HS256 la misma clave firma y verifica — cualquier servicio que necesite verificar tokens también podría emitirlos. Con RS256, solo el servicio con la clave privada puede firmar. Cualquier otro servicio solo necesita la clave pública para verificar. Es la estrategia correcta para sistemas distribuidos y microservicios. Basicamente, prefiero usar una clave asimétrica que una simétrica.

### Estrategia de testing en 3 capas
- **Unit tests**: lógica de negocio con mocks, rápidos, sin I/O
- **Repository tests**: integración real contra PostgreSQL via Testcontainers, validan queries JPQL y constraints
- **HTTP tests**: end-to-end con REST Assured, validan el pipeline completo incluyendo serialización, autenticación y códigos HTTP

Esta estrategia asegura cobertura en cada capa sin duplicar esfuerzo.

---

## 📄 Licencia

MIT © [nicolasjitorres](https://github.com/nicolasjitorres)
