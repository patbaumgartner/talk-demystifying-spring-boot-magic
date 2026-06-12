-- V2: Spring Modulith JDBC event publication outbox (schema v2 / PostgreSQL)
--
-- This table is the transactional outbox for cross-module domain events
-- (ClaimSubmittedEvent, ClaimApprovedEvent, ClaimRejectedEvent).
-- Events are written here inside the originating transaction and delivered
-- to listeners after commit, guaranteeing at-least-once delivery.
--
-- DDL sourced from spring-modulith-events-jdbc schemas/v2/schema-postgresql.sql

CREATE TABLE IF NOT EXISTS event_publication
(
    id                     UUID                     NOT NULL,
    listener_id            TEXT                     NOT NULL,
    event_type             TEXT                     NOT NULL,
    serialized_event       TEXT                     NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx
    ON event_publication USING hash (serialized_event);

CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx
    ON event_publication (completion_date);
