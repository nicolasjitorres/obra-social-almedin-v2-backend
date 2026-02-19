CREATE TABLE IF NOT EXISTS admins (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    dni         VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    role        VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS affiliates (
    id                    BIGSERIAL PRIMARY KEY,
    first_name            VARCHAR(255) NOT NULL,
    last_name             VARCHAR(255) NOT NULL,
    dni                   VARCHAR(255) NOT NULL UNIQUE,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    role                  VARCHAR(50)  NOT NULL,
    password              VARCHAR(255) NOT NULL,
    active                BOOLEAN      NOT NULL DEFAULT TRUE,
    health_insurance_code VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS specialists (
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    dni        VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    role       VARCHAR(50)  NOT NULL,
    password   VARCHAR(255) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    speciality VARCHAR(100) NOT NULL,
    address    VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS schedules (
    id             BIGSERIAL PRIMARY KEY,
    specialist_id  BIGINT       NOT NULL REFERENCES specialists(id),
    day_of_week    VARCHAR(20)  NOT NULL,
    start_time     TIME         NOT NULL,
    end_time       TIME         NOT NULL,
    slot_duration  INTEGER      NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS specialist_unavailability (
    id            BIGSERIAL PRIMARY KEY,
    specialist_id BIGINT  NOT NULL REFERENCES specialists(id),
    date_from     DATE    NOT NULL,
    date_to       DATE,
    start_time    TIME,
    end_time      TIME,
    reason        VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS appointments (
    id                    BIGSERIAL PRIMARY KEY,
    affiliate_id          BIGINT       NOT NULL REFERENCES affiliates(id),
    specialist_id         BIGINT       NOT NULL REFERENCES specialists(id),
    date                  DATE         NOT NULL,
    start_time            TIME         NOT NULL,
    end_time              TIME         NOT NULL,
    duration_minutes      INTEGER      NOT NULL,
    type                  VARCHAR(50)  NOT NULL,
    status                VARCHAR(50)  NOT NULL DEFAULT 'PENDIENTE',
    cancelled_by          VARCHAR(50),
    cancellation_reason   VARCHAR(255),
    clinical_notes        TEXT,
    prescription          TEXT,
    penalty_applied       BOOLEAN      NOT NULL DEFAULT FALSE,
    reminder_sent         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP    NOT NULL,
    parent_appointment_id BIGINT REFERENCES appointments(id)
);

CREATE TABLE IF NOT EXISTS affiliate_penalties (
    id              BIGSERIAL PRIMARY KEY,
    affiliate_id    BIGINT    NOT NULL REFERENCES affiliates(id),
    appointment_id  BIGINT    NOT NULL REFERENCES appointments(id),
    applied_at      TIMESTAMP NOT NULL,
    suspended_until TIMESTAMP,
    active          BOOLEAN   NOT NULL DEFAULT TRUE
);