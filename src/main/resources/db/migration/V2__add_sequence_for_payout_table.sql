CREATE TABLE sink_drinker.sequence_for_payout
(
    payout_id   CHARACTER VARYING NOT NULL,
    sequence_id INTEGER           NOT NULL,
    CONSTRAINT sequence_for_payout_pkey PRIMARY KEY (payout_id)
);
