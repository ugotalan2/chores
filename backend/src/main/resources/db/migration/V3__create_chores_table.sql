CREATE TABLE chores (
                        chore_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name              VARCHAR(200) NOT NULL,
                        description       TEXT,

                        verification_type VARCHAR(20) NOT NULL
                            CHECK (verification_type IN ('PHOTO', 'CHECKBOX')),

                        ai_mode           VARCHAR(30)
                            CHECK (ai_mode IN (
                                               'REFERENCE_COMPARISON',
                                               'CLUTTER_DETECTION'
                                )),

                        chore_block       VARCHAR(30) NOT NULL
                            CHECK (chore_block IN (
                                                   'DAILY_MORNING',
                                                   'BREAKFAST_LNT',
                                                   'LUNCH_LNT',
                                                   'DINNER_KITCHEN',
                                                   'SATURDAY',
                                                   'LAUNDRY'
                                )),

                        assignment_type   VARCHAR(30) NOT NULL
                            CHECK (assignment_type IN (
                                                       'COMMON',
                                                       'KITCHEN_ROTATION',
                                                       'HOUSE_ROTATION',
                                                       'SATURDAY_HOUSEHOLD',
                                                       'SATURDAY_BATHROOM',
                                                       'DINNER_ROLE',
                                                       'INDIVIDUAL'
                                )),

                        is_active         BOOLEAN NOT NULL DEFAULT TRUE,

                        instructions      JSONB,
                        supplies          JSONB,

                        created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                        updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                        created_by        UUID NOT NULL
                            REFERENCES users(user_id)
);

CREATE INDEX idx_chores_block
    ON chores(chore_block);
CREATE INDEX idx_chores_assignment_type
    ON chores(assignment_type);
CREATE INDEX idx_chores_is_active
    ON chores(is_active);