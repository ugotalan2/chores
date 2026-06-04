CREATE TABLE required_shots (
                                shot_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                chore_id             UUID NOT NULL
                                    REFERENCES chores(chore_id)
                                        ON DELETE CASCADE,
                                label                VARCHAR(200) NOT NULL,
                                hint_text            TEXT,
                                reference_photo_url  VARCHAR(500),
                                ai_prompt            TEXT,
                                sort_order           INTEGER NOT NULL DEFAULT 0,

                                created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_required_shots_chore_id
    ON required_shots(chore_id);