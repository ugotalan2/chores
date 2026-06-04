CREATE TABLE notifications (
                               notification_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               notification_type VARCHAR(30) NOT NULL
                                   CHECK (notification_type IN (
                                                                'DISPUTE_REVIEW',
                                                                'CHORE_APPROVED',
                                                                'CHORE_REJECTED',
                                                                'GROUNDING_END'
                                       )),

                               sent_to_user_ids  UUID[] NOT NULL,

                               instance_id       UUID
                                   REFERENCES daily_chore_instances(instance_id),
                               submission_id     UUID
                                   REFERENCES shot_submissions(submission_id),
                               from_user_id      UUID
                                   REFERENCES users(user_id),

                               title             VARCHAR(200) NOT NULL,
                               body              TEXT NOT NULL,
                               deep_link_path    VARCHAR(500),

                               sent_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                               read_at           TIMESTAMP WITH TIME ZONE,

                               created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_sent_to
    ON notifications USING GIN(sent_to_user_ids);
CREATE INDEX idx_notifications_unread
    ON notifications(read_at)
    WHERE read_at IS NULL;
CREATE INDEX idx_notifications_from_user
    ON notifications(from_user_id, notification_type);