CREATE TABLE shot_submissions (
                                  submission_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  instance_id      UUID NOT NULL
                                      REFERENCES daily_chore_instances(instance_id)
                                          ON DELETE CASCADE,
                                  shot_id          UUID NOT NULL
                                      REFERENCES required_shots(shot_id),

                                  photo_url        VARCHAR(500) NOT NULL,
                                  thumbnail_url    VARCHAR(500),
                                  captured_at      TIMESTAMP WITH TIME ZONE NOT NULL,

                                  exif_timestamp   TIMESTAMP WITH TIME ZONE,
                                  exif_validated   BOOLEAN,

                                  ai_status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                      CHECK (ai_status IN (
                                                           'PENDING',
                                                           'AI_PASSED',
                                                           'AI_FAILED',
                                                           'AWAITING_REVIEW',
                                                           'PARENT_APPROVED',
                                                           'PARENT_REJECTED'
                                          )),
                                  ai_verdict       VARCHAR(10)
                                      CHECK (ai_verdict IN ('PASS', 'FAIL')),
                                  ai_reason        TEXT,
                                  ai_processed_at  TIMESTAMP WITH TIME ZONE,

                                  disputed_by_kid  BOOLEAN DEFAULT FALSE,
                                  disputed_at      TIMESTAMP WITH TIME ZONE,

                                  overridden_by    UUID
                                      REFERENCES users(user_id),
                                  overridden_at    TIMESTAMP WITH TIME ZONE,
                                  override_note    TEXT,

                                  attempt_number   INTEGER NOT NULL DEFAULT 1,

                                  created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                  updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_submissions_instance_id
    ON shot_submissions(instance_id);
CREATE INDEX idx_submissions_shot_id
    ON shot_submissions(shot_id);
CREATE INDEX idx_submissions_ai_status
    ON shot_submissions(ai_status);
CREATE INDEX idx_submissions_disputed
    ON shot_submissions(disputed_by_kid)
    WHERE disputed_by_kid = TRUE;