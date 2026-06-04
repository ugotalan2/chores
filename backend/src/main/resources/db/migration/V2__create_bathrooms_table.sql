CREATE TABLE bathrooms (
                           bathroom_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           name           VARCHAR(100) NOT NULL,
                           description    VARCHAR(500),
                           is_active      BOOLEAN NOT NULL DEFAULT TRUE,

                           created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                           updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_bathroom
        FOREIGN KEY (assigned_bathroom_id)
            REFERENCES bathrooms(bathroom_id);