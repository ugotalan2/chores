CREATE TABLE rotation_history (
                                  history_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  rotation_type         VARCHAR(30) NOT NULL
                                      CHECK (rotation_type IN (
                                                               'KITCHEN',
                                                               'HOUSE',
                                                               'DINNER_ROLES',
                                                               'SATURDAY_HOUSEHOLD',
                                                               'SATURDAY_BATHROOM'
                                          )),

                                  effective_date        TIMESTAMP WITH TIME ZONE NOT NULL,

                                  previous_assignments  JSONB NOT NULL,
                                  new_assignments       JSONB NOT NULL,

                                  triggered_by          UUID NOT NULL
                                      REFERENCES users(user_id),
                                  notes                 TEXT,

                                  created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rotation_history_type_date
    ON rotation_history(rotation_type, effective_date);