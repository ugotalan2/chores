CREATE TABLE assignments (
                             assignment_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id              UUID NOT NULL
                                 REFERENCES users(user_id),
                             chore_id             UUID NOT NULL
                                 REFERENCES chores(chore_id),
                             assignment_type      VARCHAR(30) NOT NULL,

                             rotation_start_date  TIMESTAMP WITH TIME ZONE,
                             rotation_end_date    TIMESTAMP WITH TIME ZONE,

                             current_room_index   INTEGER DEFAULT 0,
                             current_task_index   INTEGER DEFAULT 0,

                             last_rotated_at      TIMESTAMP WITH TIME ZONE,

                             is_summer_variation  BOOLEAN DEFAULT FALSE,
                             summer_role_override VARCHAR(200),

                             is_active            BOOLEAN NOT NULL DEFAULT TRUE,

                             created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                             updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                             created_by           UUID NOT NULL
                                 REFERENCES users(user_id)
);

CREATE INDEX idx_assignments_user_id
    ON assignments(user_id);
CREATE INDEX idx_assignments_chore_id
    ON assignments(chore_id);
CREATE INDEX idx_assignments_type_active
    ON assignments(assignment_type, is_active);