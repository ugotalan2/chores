CREATE TABLE streaks (
                         user_id              UUID PRIMARY KEY
                             REFERENCES users(user_id),
                         current_streak       INTEGER NOT NULL DEFAULT 0,
                         longest_streak       INTEGER NOT NULL DEFAULT 0,
                         last_completed_date  DATE,
                         streak_start_date    DATE,

                         updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);