CREATE SCHEMA IF NOT EXISTS sink_drinker;

CREATE TABLE IF NOT EXISTS sink_drinker.last_event
(
    sink_id CHARACTER VARYING NOT NULL,
    id      BIGINT            NOT NULL,
    CONSTRAINT last_event_pkey PRIMARY KEY (sink_id)
);