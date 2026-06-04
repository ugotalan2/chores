CREATE TABLE schedule_config (
                                 config_id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 name                      VARCHAR(100) NOT NULL,
                                 is_active                 BOOLEAN NOT NULL DEFAULT FALSE,
                                 schedule_type             VARCHAR(30) NOT NULL DEFAULT 'STANDARD',

                                 screen_time_gate_enabled  BOOLEAN DEFAULT TRUE,
                                 screen_time_gate_hour     INTEGER DEFAULT 14,

                                 active_blocks             TEXT[] NOT NULL,

                                 saturday_block_active     BOOLEAN NOT NULL DEFAULT TRUE,
                                 sunday_kitchen_only       BOOLEAN NOT NULL DEFAULT TRUE,
                                 sunday_enforced           BOOLEAN NOT NULL DEFAULT FALSE,

                                 valid_from                TIMESTAMP WITH TIME ZONE,
                                 valid_to                  TIMESTAMP WITH TIME ZONE,

                                 created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                 updated_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                 created_by                UUID NOT NULL
                                     REFERENCES users(user_id)
);

CREATE INDEX idx_schedule_config_is_active
    ON schedule_config(is_active);

CREATE UNIQUE INDEX idx_schedule_config_single_active
    ON schedule_config(is_active)
    WHERE is_active = TRUE;