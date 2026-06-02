# DATA_MODEL.md

## Overview
ChoreHouse uses PostgreSQL hosted on Railway.
All schema changes are managed through Flyway
versioned migration files. Never alter the database
manually — always through a new migration file.

This app's timezone: America/Chicago (CST/CDT)
All TIMESTAMP columns use TIMESTAMP WITH TIME ZONE.
All date-only columns use DATE type (no timezone shift).
See GOTCHAS.md GOTCHA-001 for full timezone setup.

---

## Tables

---

### users
Stores both parent and kid profiles.

```sql
-- V1__create_users_table.sql

CREATE TABLE users (
  user_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  display_name   VARCHAR(100) NOT NULL,
  role           VARCHAR(20)  NOT NULL
                 CHECK (role IN ('PARENT', 'CHILD')),
  avatar_url     VARCHAR(500),
  is_active      BOOLEAN NOT NULL DEFAULT TRUE,

  -- CHILD only (nullable for parents)
  age                     INTEGER,
  assigned_bathroom_id    UUID,         -- FK added after bathrooms table
  laundry_day_of_week     INTEGER       -- 0=Sun, 1=Mon ... 6=Sat
                          CHECK (laundry_day_of_week BETWEEN 0 AND 6),

  -- PARENT only (nullable for children)
  email           VARCHAR(255) UNIQUE,
  clerk_user_id   VARCHAR(255) UNIQUE,  -- Clerk OAuth user ID
  fcm_token       VARCHAR(500),         -- Firebase push notification token
  is_primary_admin BOOLEAN DEFAULT FALSE,

  created_at     TIMESTAMP WITH TIME ZONE NOT NULL
                 DEFAULT NOW(),
  updated_at     TIMESTAMP WITH TIME ZONE NOT NULL
                 DEFAULT NOW(),
  created_by     UUID                     -- FK to users.user_id
);

CREATE INDEX idx_users_role
  ON users(role);
CREATE INDEX idx_users_clerk_id
  ON users(clerk_user_id);
CREATE INDEX idx_users_is_active
  ON users(is_active);
```

---

### bathrooms
Defines the bathrooms in the house that kids
are assigned to for Saturday cleaning rotation.

```sql
-- V2__create_bathrooms_table.sql

CREATE TABLE bathrooms (
  bathroom_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name           VARCHAR(100) NOT NULL,  -- "Main Kids Bathroom"
  description    VARCHAR(500),
  is_active      BOOLEAN NOT NULL DEFAULT TRUE,

  created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Add FK now that bathrooms table exists
ALTER TABLE users
  ADD CONSTRAINT fk_users_bathroom
  FOREIGN KEY (assigned_bathroom_id)
  REFERENCES bathrooms(bathroom_id);
```

---

### chores
Master chore definitions. Created and managed
by parents. These are templates — not daily
assignments.

```sql
-- V3__create_chores_table.sql

CREATE TABLE chores (
  chore_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             VARCHAR(200) NOT NULL,
  description      TEXT,

  verification_type VARCHAR(20) NOT NULL
    CHECK (verification_type IN ('PHOTO', 'CHECKBOX')),

  ai_mode          VARCHAR(30)
    CHECK (ai_mode IN (
      'REFERENCE_COMPARISON',
      'CLUTTER_DETECTION'
    )),
    -- NULL for checkbox chores

  chore_block      VARCHAR(30) NOT NULL
    CHECK (chore_block IN (
      'DAILY_MORNING',
      'BREAKFAST_LNT',   -- leave no trace
      'LUNCH_LNT',       -- leave no trace
      'DINNER_KITCHEN',
      'SATURDAY',
      'LAUNDRY'
    )),

  assignment_type  VARCHAR(30) NOT NULL
    CHECK (assignment_type IN (
      'COMMON',              -- all kids do this chore
      'KITCHEN_ROTATION',    -- one kid assigned
      'HOUSE_ROTATION',      -- one kid assigned
      'SATURDAY_HOUSEHOLD',  -- one room per kid
      'SATURDAY_BATHROOM',   -- per assigned bathroom
      'DINNER_ROLE',         -- fixed role per kid
      'INDIVIDUAL'           -- laundry, per kid
    )),

  is_active        BOOLEAN NOT NULL DEFAULT TRUE,

  -- V2 fields (nullable in v1, structure ready)
  instructions     JSONB,  -- [{step_number, text}]
  supplies         JSONB,  -- [{name, notes}]

  created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  created_by       UUID NOT NULL
                   REFERENCES users(user_id)
);

CREATE INDEX idx_chores_block
  ON chores(chore_block);
CREATE INDEX idx_chores_assignment_type
  ON chores(assignment_type);
CREATE INDEX idx_chores_is_active
  ON chores(is_active);
```

---

### required_shots
Defines the photo shots required to complete
a chore. One row per required shot per chore.

```sql
-- V4__create_required_shots_table.sql

CREATE TABLE required_shots (
  shot_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  chore_id             UUID NOT NULL
                       REFERENCES chores(chore_id)
                       ON DELETE CASCADE,
  label                VARCHAR(200) NOT NULL,
                       -- "Living Room - Main Area"
  hint_text            TEXT,
                       -- "Stand in the doorway facing the couch"
  reference_photo_url  VARCHAR(500),
                       -- Supabase Storage or S3 URL
  ai_prompt            TEXT,
                       -- Passed to GPT-4o Vision API
  sort_order           INTEGER NOT NULL DEFAULT 0,

  created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_required_shots_chore_id
  ON required_shots(chore_id);
```

---

### assignments
Tracks which kid is assigned to which
rotation chore. One row per kid per
rotation type.

```sql
-- V5__create_assignments_table.sql

CREATE TABLE assignments (
  assignment_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id              UUID NOT NULL
                       REFERENCES users(user_id),
  chore_id             UUID NOT NULL
                       REFERENCES chores(chore_id),
  assignment_type      VARCHAR(30) NOT NULL,
                       -- mirrors chores.assignment_type

  rotation_start_date  TIMESTAMP WITH TIME ZONE,
  rotation_end_date    TIMESTAMP WITH TIME ZONE,
                       -- NULL = manual trigger only

  -- Saturday household room rotation
  current_room_index   INTEGER DEFAULT 0,
                       -- 0-4, cycles monthly

  -- Saturday bathroom task rotation
  current_task_index   INTEGER DEFAULT 0,
                       -- 0-3, cycles weekly

  last_rotated_at      TIMESTAMP WITH TIME ZONE,

  -- Dinner role summer variation
  is_summer_variation  BOOLEAN DEFAULT FALSE,
  summer_role_override VARCHAR(200),
                       -- e.g. "wipe counters"

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
```

---

### schedule_config
Defines the active schedule configuration.
V1 has one active config (summer/standard mode).
Structured ready for v2 school/non-school logic.

```sql
-- V6__create_schedule_config_table.sql

CREATE TABLE schedule_config (
  config_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name                 VARCHAR(100) NOT NULL,
                       -- "Summer 2026"
  is_active            BOOLEAN NOT NULL DEFAULT FALSE,

  -- V1 always 'STANDARD'
  -- V2 adds 'SCHOOL_DAY' | 'NON_SCHOOL_DAY'
  schedule_type        VARCHAR(30) NOT NULL DEFAULT 'STANDARD',

  -- Screen time soft gate (tracked, not enforced by app)
  screen_time_gate_enabled  BOOLEAN DEFAULT TRUE,
  screen_time_gate_hour     INTEGER DEFAULT 14,
                            -- 14 = 2pm

  -- Which chore blocks active for this schedule
  active_blocks        TEXT[] NOT NULL,
                       -- e.g. ARRAY['DAILY_MORNING',
                       --            'BREAKFAST_LNT',
                       --            'LUNCH_LNT',
                       --            'DINNER_KITCHEN']

  saturday_block_active    BOOLEAN NOT NULL DEFAULT TRUE,
  sunday_kitchen_only      BOOLEAN NOT NULL DEFAULT TRUE,
  sunday_enforced          BOOLEAN NOT NULL DEFAULT FALSE,

  valid_from           TIMESTAMP WITH TIME ZONE,
  valid_to             TIMESTAMP WITH TIME ZONE,
                       -- NULL = indefinite

  created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  created_by           UUID NOT NULL
                       REFERENCES users(user_id)
);

CREATE INDEX idx_schedule_config_is_active
  ON schedule_config(is_active);

-- Enforce only one active config at a time
CREATE UNIQUE INDEX idx_schedule_config_single_active
  ON schedule_config(is_active)
  WHERE is_active = TRUE;
```

---

### daily_chore_instances
Generated daily for each kid based on their
assignments and active schedule. This is the
working record for a given day.

```sql
-- V7__create_daily_chore_instances_table.sql

CREATE TABLE daily_chore_instances (
  instance_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id          UUID NOT NULL
                   REFERENCES users(user_id),
  chore_id         UUID NOT NULL
                   REFERENCES chores(chore_id),
  assignment_id    UUID
                   REFERENCES assignments(assignment_id),

  -- DATE type — no timezone shift (see GOTCHAS-001)
  date             DATE NOT NULL,
  day_type         VARCHAR(20) NOT NULL
                   CHECK (day_type IN (
                     'WEEKDAY',
                     'SATURDAY',
                     'SUNDAY'
                   )),
  chore_block      VARCHAR(30) NOT NULL,

  status           VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
                   CHECK (status IN (
                     'NOT_STARTED',
                     'IN_PROGRESS',
                     'AWAITING_REVIEW',
                     'DONE'
                   )),

  -- Checkbox chores
  is_checked       BOOLEAN DEFAULT FALSE,
  checked_at       TIMESTAMP WITH TIME ZONE,

  -- Timing
  started_at       TIMESTAMP WITH TIME ZONE,
  completed_at     TIMESTAMP WITH TIME ZONE,

  -- Dispute tracking
  disputed_at      TIMESTAMP WITH TIME ZONE,
  dispute_note     TEXT,

  -- Parent review
  parent_reviewed_by  UUID
                      REFERENCES users(user_id),
  parent_reviewed_at  TIMESTAMP WITH TIME ZONE,
  parent_approved     BOOLEAN,
  parent_note         TEXT,

  -- Carry-over for missed laundry day
  is_carried_over  BOOLEAN DEFAULT FALSE,
  original_date    DATE,

  -- Dinner block flag for next-day device check
  affects_next_day_device  BOOLEAN DEFAULT FALSE,

  created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
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
```

---

### shot_submissions
One row per photo submission attempt per
required shot per daily instance.

```sql
-- V8__create_shot_submissions_table.sql

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

  -- V3 EXIF validation (nullable in v1)
  exif_timestamp   TIMESTAMP WITH TIME ZONE,
  exif_validated   BOOLEAN,

  -- AI verification
  ai_status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                   CHECK (ai_status IN (
                     'PENDING',
                     'AI_PASSED',
                     'AI_FAILED',
                     'AWAITING_REVIEW',
                     'PARENT_APPROVED',
                     'PARENT_REJECTED'  -- v2
                   )),
  ai_verdict       VARCHAR(10)
                   CHECK (ai_verdict IN ('PASS', 'FAIL')),
  ai_reason        TEXT,
  ai_processed_at  TIMESTAMP WITH TIME ZONE,

  -- Dispute
  disputed_by_kid  BOOLEAN DEFAULT FALSE,
  disputed_at      TIMESTAMP WITH TIME ZONE,

  -- Parent override
  overridden_by    UUID
                   REFERENCES users(user_id),
  overridden_at    TIMESTAMP WITH TIME ZONE,
  override_note    TEXT,

  -- V2 strike tracking (stored now, enforced in v2)
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
```

---

### rotation_history
Tracks every rotation event for auditing
and parent dashboard display.

```sql
-- V9__create_rotation_history_table.sql

CREATE TABLE rotation_history (
  history_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  rotation_type    VARCHAR(30) NOT NULL
                   CHECK (rotation_type IN (
                     'KITCHEN',
                     'HOUSE',
                     'DINNER_ROLES',
                     'SATURDAY_HOUSEHOLD',
                     'SATURDAY_BATHROOM'
                   )),

  effective_date   TIMESTAMP WITH TIME ZONE NOT NULL,

  -- Snapshots before and after rotation
  previous_assignments  JSONB NOT NULL,
                        -- [{user_id, chore_id, role}]
  new_assignments       JSONB NOT NULL,

  triggered_by     UUID NOT NULL
                   REFERENCES users(user_id),
  notes            TEXT,

  created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rotation_history_type_date
  ON rotation_history(rotation_type, effective_date);
```

---

### streaks
Tracks consecutive day completion streaks
per kid. One row per kid.

```sql
-- V10__create_streaks_table.sql

CREATE TABLE streaks (
  user_id              UUID PRIMARY KEY
                       REFERENCES users(user_id),
  current_streak       INTEGER NOT NULL DEFAULT 0,
  longest_streak       INTEGER NOT NULL DEFAULT 0,
  -- DATE type — no timezone shift
  last_completed_date  DATE,
  streak_start_date    DATE,

  updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
```

---

### notifications
Log of all push notifications sent.

```sql
-- V11__create_notifications_table.sql

CREATE TABLE notifications (
  notification_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  notification_type VARCHAR(30) NOT NULL
                    CHECK (notification_type IN (
                      'DISPUTE_REVIEW',
                      'CHORE_APPROVED',   -- v2
                      'CHORE_REJECTED',   -- v2
                      'GROUNDING_END'     -- v2
                    )),

  -- Stored as array of parent user_ids
  sent_to_user_ids UUID[] NOT NULL,

  -- Related records
  instance_id      UUID
                   REFERENCES daily_chore_instances(instance_id),
  submission_id    UUID
                   REFERENCES shot_submissions(submission_id),
  from_user_id     UUID
                   REFERENCES users(user_id),

  title            VARCHAR(200) NOT NULL,
  body             TEXT NOT NULL,
  deep_link_path   VARCHAR(500),

  sent_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  read_at          TIMESTAMP WITH TIME ZONE,

  created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_sent_to
  ON notifications USING GIN(sent_to_user_ids);
CREATE INDEX idx_notifications_unread
  ON notifications(read_at)
  WHERE read_at IS NULL;
CREATE INDEX idx_notifications_from_user
  ON notifications(from_user_id, notification_type);
```

---

### device_sessions (v3)
Tracks chore device dock check-in/check-out
for dock alarm system.

```sql
-- V12__create_device_sessions_table.sql (v3)

CREATE TABLE device_sessions (
  session_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id          VARCHAR(255) NOT NULL,
  user_id            UUID NOT NULL
                     REFERENCES users(user_id),

  docked_at          TIMESTAMP WITH TIME ZONE,
  undocked_at        TIMESTAMP WITH TIME ZONE,
  redocked_at        TIMESTAMP WITH TIME ZONE,

  alarm_triggered    BOOLEAN DEFAULT FALSE,
  alarm_triggered_at TIMESTAMP WITH TIME ZONE,
  device_locked_at   TIMESTAMP WITH TIME ZONE,

  created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_device_sessions_device_id
  ON device_sessions(device_id);
CREATE INDEX idx_device_sessions_user_id
  ON device_sessions(user_id);
```

---

### grounding_records (v2)
Tracks grounding periods per kid with exact
end date and time to eliminate disputes.

```sql
-- V13__create_grounding_records_table.sql (v2)

CREATE TABLE grounding_records (
  grounding_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        UUID NOT NULL
                 REFERENCES users(user_id),
  reason         TEXT NOT NULL,
  start_date     TIMESTAMP WITH TIME ZONE NOT NULL,
  end_date       TIMESTAMP WITH TIME ZONE NOT NULL,
                 -- exact date AND time, no ambiguity
  is_active      BOOLEAN NOT NULL DEFAULT TRUE,

  created_by     UUID NOT NULL
                 REFERENCES users(user_id),
  created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_grounding_user_active
  ON grounding_records(user_id, is_active)
  WHERE is_active = TRUE;
```

---

## Key Relationships

```
users (PARENT)
  ├── creates → chores
  ├── creates → assignments
  ├── manages → schedule_config
  ├── reviews → daily_chore_instances
  └── triggers → rotation_history

users (CHILD)
  ├── assigned_bathroom_id → bathrooms
  ├── has many → assignments
  ├── has many → daily_chore_instances (generated daily)
  └── has one → streaks

chores
  ├── has many → required_shots
  ├── referenced by → assignments
  └── referenced by → daily_chore_instances

daily_chore_instances
  ├── has many → shot_submissions
  ├── references → chores
  ├── references → assignments
  └── references → users (CHILD)

assignments
  ├── references → users (CHILD)
  ├── references → chores
  └── history tracked in → rotation_history
```

---

## Flyway Migration File Order

```
V1__create_users_table.sql
V2__create_bathrooms_table.sql
V3__create_chores_table.sql
V4__create_required_shots_table.sql
V5__create_assignments_table.sql
V6__create_schedule_config_table.sql
V7__create_daily_chore_instances_table.sql
V8__create_shot_submissions_table.sql
V9__create_rotation_history_table.sql
V10__create_streaks_table.sql
V11__create_notifications_table.sql

-- V2 migrations (create when building v2)
V12__create_grounding_records_table.sql

-- V3 migrations (create when building v3)
V13__create_device_sessions_table.sql
```

Note: V12 and V13 filenames above are placeholders.
Renumber sequentially when actually created to avoid
gaps in Flyway version sequence.

---

## Daily Instance Generation Logic

Spring Boot scheduled job fires at midnight CST
(see GOTCHAS-002 for timezone configuration):

```
@Scheduled(cron = "0 0 0 * * *",
           zone = "America/Chicago")
```

1. Read active schedule_config (is_active = TRUE)
2. Determine day type for today
   (WEEKDAY / SATURDAY / SUNDAY)
3. For each active CHILD in users:
   a. Get their active assignments
   b. Get COMMON chores for this day type
   c. INSERT daily_chore_instances for each chore
   d. Check for carried-over laundry
      (is_carried_over = TRUE from previous day)
   e. If laundry carry-over exists, include it
      with original_date preserved
4. Advance Saturday household room index if Saturday
   and first Saturday of month
5. Advance Saturday bathroom task index if Saturday

---

## Rotation Logic

### Saturday Household Room (Monthly)
```sql
-- When parent triggers rotation:
UPDATE assignments
SET current_room_index = (current_room_index + 1) % 5,
    last_rotated_at = NOW()
WHERE assignment_type = 'SATURDAY_HOUSEHOLD'
AND is_active = TRUE;
```

### Saturday Bathroom Tasks (Weekly)
```sql
-- Auto-advances every Saturday:
UPDATE assignments
SET current_task_index = (current_task_index + 1) % 4,
    last_rotated_at = NOW()
WHERE assignment_type = 'SATURDAY_BATHROOM'
AND is_active = TRUE;
```

### Kitchen / House / Dinner Roles (Annual)
Manual trigger only — parent clicks rotate
in dashboard. Spring Boot reassigns kids to
next position in rotation order, writes
rotation_history record.

---

## Storage — File URL Conventions

```
Reference photos:
  /reference-photos/{chore_id}/{shot_id}/reference.jpg

Submissions:
  /submissions/{user_id}/{date}/{instance_id}/{submission_id}.jpg

Thumbnails:
  /thumbnails/{user_id}/{date}/{instance_id}/{submission_id}_thumb.jpg

Avatars:
  /avatars/{user_id}/avatar.jpg
```

Files stored in Railway-connected storage or
Cloudflare R2 (free tier, S3-compatible).
Database stores URL strings only — never binary data.

---

## Notes on V2 Readiness

Fields included in v1 schema but not used until v2:

- schedule_config.schedule_type — always 'STANDARD' in v1
- chores.instructions and chores.supplies — NULL in v1
- shot_submissions.exif_timestamp — NULL until v3
- shot_submissions.attempt_number — stored, not enforced
- shot_submissions ai_status 'PARENT_REJECTED' — stored,
  not used until v2 rejection flow is built
- grounding_records table — migration file written in v2
- device_sessions table — migration file written in v3