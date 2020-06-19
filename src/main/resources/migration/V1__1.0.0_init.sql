CREATE SCHEMA IF NOT EXISTS sink_drinker;

CREATE TABLE IF NOT EXISTS sink_drinker.last_event
(
    sink_id       CHARACTER VARYING NOT NULL,
    last_event_id BIGINT            NOT NULL,
    CONSTRAINT offset_pkey PRIMARY KEY (sink_id)
);