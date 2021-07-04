CREATE TABLE sink_drinker.payout_snapshot
(
    payout_id CHARACTER VARYING NOT NULL,
    snapshot  CHARACTER VARYING NOT NULL,
    sequence_id INTEGER           NOT NULL,
    CONSTRAINT payout_snapshot_pkey PRIMARY KEY (payout_id)
);
