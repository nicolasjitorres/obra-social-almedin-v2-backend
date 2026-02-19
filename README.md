# 🏥 Obra Social Almedin — Backend v2

Backend REST de un sistema de gestión de obra social médica, desarrollado con **Quarkus 3** siguiendo **arquitectura hexagonal**. Permite la gestión de afiliados, especialistas, turnos, horarios y autenticación JWT con roles.

[![API Live](https://img.shields.io/badge/API-Live-brightgreen?logo=render)](https://obra-social-almedin-v2-backend-latest.onrender.com/q/swagger-ui)
[![Swagger UI](https://img.shields.io/badge/Swagger-UI-85EA2D?logo=swagger&logoColor=black)](https://obra-social-almedin-v2-backend-latest.onrender.com/q/swagger-ui)
[![Coverage](https://codecov.io/gh/nicolasjitorres/obra-social-almedin-v2-backend/branch/main/graph/badge.svg?token=XXXXXX)](https://codecov.io/gh/nicolasjitorres/obra-social-almedin-v2-backend)
![CI](https://github.com/nicolasjitorres/obra-social-almedin-v2-backend/actions/workflows/ci.yml/badge.svg)
![CD](https://github.com/nicolasjitorres/obra-social-almedin-v2-backend/actions/workflows/cd.yml/badge.svg)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://www.oracle.com/java/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.31.3-blue?logo=quarkus)](https://quarkus.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/Tests-112%20passing-brightgreen?logo=junit5)](https://github.com/nicolasjitorres/obra-social-almedin-v2-backend)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Tech Stack](#-tech-stack)
- [Arquitectura](#-arquitectura)
- [Diagrama de Clases](#-diagrama-de-clases)
- [DER — Diagrama Entidad-Relación](#-der--diagrama-entidad-relación)
- [Casos de Uso](#-casos-de-uso)
- [Endpoints](#-endpoints)
- [Autenticación JWT](#-autenticación-jwt)
- [Seguridad](#-seguridad)
- [Notificaciones por Email](#-notificaciones-por-email)
- [Cómo correr el proyecto](#-cómo-correr-el-proyecto)
- [Tests](#-tests)
- [CI/CD](#-cicd)
- [Decisiones Técnicas](#-decisiones-técnicas)
---

## 📌 Descripción

Sistema backend para una obra social médica que gestiona:

- **Afiliados**: alta, baja lógica, consulta y modificación
- **Especialistas**: gestión de profesionales médicos con especialidades
- **Turnos**: reserva, cancelación, confirmación y seguimiento de citas médicas
- **Horarios**: configuración de disponibilidad semanal de especialistas
- **Penalidades**: suspensión automática de afiliados por ausencias reiteradas
- **Autenticación**: JWT con roles diferenciados (AFFILIATE, SPECIALIST, ADMIN)
- **Notificaciones**: recordatorios automáticos de turno por email con scheduler configurable
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
| Email | Quarkus Mailer (SMTP / mock) |
| Scheduler | Quarkus Scheduler (cron) |
| Contenedores | Docker + Docker Compose |
| Tests | JUnit 5 + Mockito + REST Assured |
| Contenedores de test | Testcontainers |
| Cobertura | JaCoCo |
| CI | GitHub Actions |
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

## 📧 Notificaciones por Email

El sistema incluye un **scheduler de recordatorios** que corre todos los días a las 8am y envía emails a los afiliados con turnos pendientes dentro de las próximas 24 horas.

### Configuración del scheduler

```properties
# Cuántas horas antes del turno enviar el recordatorio
almedin.reminder.hours-before=24

# Expresión cron (por defecto: todos los días a las 8am)
almedin.reminder.cron=0 0 8 * * ?
```

### Comportamiento por perfil

| Perfil | Comportamiento |
|--------|---------------|
| `dev` | Mock mailer — los emails se loguean en consola, no se envían |
| `test` | Mock mailer + scheduler deshabilitado para no interferir con tests |
| `prod` | SMTP real configurable via variables de entorno |

### Variables de entorno para email

| Variable | Descripción |
|----------|-------------|
| `MAIL_HOST` | Servidor SMTP |
| `MAIL_PORT` | Puerto SMTP |
| `MAIL_USERNAME` | Usuario SMTP |
| `MAIL_PASSWORD` | Contraseña SMTP |
| `MAIL_FROM` | Dirección remitente |
---
## 🚀 Cómo correr el proyecto

### Opción 1 — Docker

Solo necesitás tener Docker instalado:

```bash
git clone https://github.com/nicolasjitorres/obra-social-almedin-v2-backend.git
cd obra-social-almedin-v2-backend
docker-compose up --build
```

Esto levanta PostgreSQL y la aplicación en un solo comando. La API queda disponible en `http://localhost:8080/api`.

> El contenedor de la app espera que la base de datos esté lista antes de iniciar (healthcheck configurado).

---

### 2 — Modo desarrollo

Requiere Java 17+ y Maven 3.9+. Quarkus levanta PostgreSQL automáticamente via Docker:

```bash
./mvnw quarkus:dev
```

La API queda disponible en `http://localhost:8080/api`  
Swagger UI en `http://localhost:8080/q/swagger-ui`

---

### Variables de entorno (producción)

| Variable | Descripción |
|----------|-------------|
| `DB_URL` | URL de conexión PostgreSQL |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `JWT_PRIVATE_KEY` | Clave privada RSA para firmar tokens |
| `JWT_PUBLIC_KEY` | Clave pública RSA para verificar tokens |
| `MAIL_HOST` | Servidor SMTP |
| `MAIL_PORT` | Puerto SMTP |
| `MAIL_USERNAME` | Usuario SMTP |
| `MAIL_PASSWORD` | Contraseña SMTP |
| `MAIL_FROM` | Dirección remitente |

### Generar claves RSA para desarrollo

```bash
# Clave privada
openssl genrsa -out private.pem 2048

# Clave pública
openssl rsa -in private.pem -pubout -out public.pem
```

---

## 📝 Tests

El proyecto cuenta con **112 tests** distribuidos en 3 niveles:

```
Tests run: 112, Failures: 0, Errors: 0, Skipped: 0
```

### Distribución por módulo

| Módulo | Unit Tests | Repository Tests | HTTP Tests | Total |
|--------|-----------|-----------------|------------|-------|
| Affiliates | 8 | 5 | 8 | 21 |
| Specialists | 7 | 5 | 9 | 21 |
| Scheduling | 13 | 10 | 19 | 42 |
| Auth | 6 | — | 10 | 16 |
| Notifications | 5 | — | — | 5 |
| Security | — | — | 7 | 7 |
| **Total** | **39** | **20** | **53** | **112** |

### Ejecutar los tests

```bash
# Todos los tests
./mvnw test

# Con reporte de cobertura JaCoCo
./mvnw verify

# Ver reporte en: target/site/jacoco/index.html
```

---

## ⚙️ CI/CD

### CI — GitHub Actions

Corre automáticamente en cada push a `main` o `dev` y en pull requests a `main`:

- Compila el proyecto con Java 17
- Corre los 99 tests incluyendo integración con Testcontainers
- Genera y publica el reporte de cobertura JaCoCo como artefacto descargable

### CD — GitHub Actions + Render

Corre automáticamente en cada push a `main`:

- Compila el JAR con Maven
- Construye la imagen Docker usando el `Dockerfile.jvm` de Quarkus
- Sube la imagen a **GitHub Container Registry (GHCR)**
- Notifica a Render via webhook para que descargue y despliegue la nueva imagen

Las claves JWT y el webhook de Render se inyectan como secrets de GitHub.

---

## 🧠 Decisiones Técnicas

### ¿Por qué Quarkus en lugar de Spring Boot?
Quarkus está diseñado para cloud-native desde el inicio: tiempo de arranque en milisegundos, menor consumo de memoria, y soporte nativo para GraalVM. Para un sistema como este, donde se podría desplegar en contenedores, esas características son relevantes. También fue una oportunidad de aprender un framework que está creciendo fuertemente en el ecosistema enterprise Java.

### ¿Por qué arquitectura hexagonal?
Porque separa el dominio de la infraestructura de forma que el código de negocio no sabe nada de Quarkus, Hibernate, ni REST. Esto permite testear la lógica de negocio en aislamiento (unit tests puros con mocks) sin levantar ningún contexto de framework.

### ¿Por qué JWT con RS256 en lugar de HS256?
Con HS256 la misma clave firma y verifica — cualquier servicio que necesite verificar tokens también podría emitirlos. Con RS256, solo el servicio con la clave privada puede firmar. Cualquier otro servicio solo necesita la clave pública para verificar. Es la estrategia correcta para sistemas distribuidos y microservicios. Basicamente, prefiero usar una clave asimétrica que una simétrica.

### ¿Por qué Quarkus Mailer con mock en dev/test?
Permite desarrollar y testear el flujo completo de notificaciones sin depender de un servidor SMTP real. En tests, el `MockMailbox` permite hacer assertions sobre los emails enviados (destinatario, asunto, contenido) como si fueran objetos, lo que hace los tests deterministas y rápidos.

### ¿Por qué Testcontainers en lugar de H2?
Los repository tests corren contra PostgreSQL real (la misma versión que producción). Esto detecta problemas de queries nativas, constraints y tipos de datos que H2 en modo de compatibilidad no reproduce fielmente.

### Estrategia de testing en 3 capas
- **Unit tests**: lógica de negocio con mocks, rápidos, sin I/O
- **Repository tests**: integración real contra PostgreSQL via Testcontainers, validan queries JPQL y constraints
- **HTTP tests**: end-to-end con REST Assured, validan el pipeline completo incluyendo serialización, autenticación y códigos HTTP

Esta estrategia asegura cobertura en cada capa sin duplicar esfuerzo.

### Estrategia de soft delete
Las entidades tienen un campo `active` en lugar de borrarse físicamente. Esto preserva la integridad referencial (los turnos históricos siguen apuntando al afiliado/especialista) y permite auditoría.

---

## 📄 Licencia

MIT © [nicolasjitorres](https://github.com/nicolasjitorres)
