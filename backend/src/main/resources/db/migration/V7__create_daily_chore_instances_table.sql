CREATE TABLE daily_chore_instances (
                                       instance_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id                  UUID NOT NULL
                                           REFERENCES users(user_id),
                                       chore_id                 UUID NOT NULL
                                           REFERENCES chores(chore_id),
                                       assignment_id            UUID
                                           REFERENCES assignments(assignment_id),

                                       date                     DATE NOT NULL,
                                       day_type                 VARCHAR(20) NOT NULL
                                           CHECK (day_type IN (
                                                               'WEEKDAY',
                                                               'SATURDAY',
                                                               'SUNDAY'
                                               )),
                                       chore_block              VARCHAR(30) NOT NULL,

                                       status                   VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
                                           CHECK (status IN (
                                                             'NOT_STARTED',
                                                             'IN_PROGRESS',
                                                             'AWAITING_REVIEW',
                                                             'DONE'
                                               )),

                                       is_checked               BOOLEAN DEFAULT FALSE,
                                       checked_at               TIMESTAMP WITH TIME ZONE,

                                       started_at               TIMESTAMP WITH TIME ZONE,
                                       completed_at             TIMESTAMP WITH TIME ZONE,

                                       disputed_at              TIMESTAMP WITH TIME ZONE,
                                       dispute_note             TEXT,

                                       parent_reviewed_by       UUID
                                           REFERENCES users(user_id),
                                       parent_reviewed_at       TIMESTAMP WITH TIME ZONE,
                                       parent_approved          BOOLEAN,
                                       parent_note              TEXT,

                                       is_carried_over          BOOLEAN DEFAULT FALSE,
                                       original_date            DATE,

                                       affects_next_day_device  BOOLEAN DEFAULT FALSE,

                                       created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                       updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_instances_user_date
    ON daily_chore_instances(user_id, date);
CREATE INDEX idx_instances_status_date
    ON daily_chore_instances(status, date);
CREATE INDEX idx_instances_block_date_user
    ON daily_chore_instances(chore_block, date, user_id);
CREATE INDEX idx_instances_next_day_device
    ON daily_chore_instances(date, affects_next_day_device)
    WHERE affects_next_day_device = TRUE;
CREATE INDEX idx_instances_carried_over
    ON daily_chore_instances(is_carried_over)
    WHERE is_carried_over = TRUE;