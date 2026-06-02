# FEATURES.md

## Version 1 (MVP — Summer 2026)

### App Lockdown
- Android kiosk mode via Device Owner / custom Capacitor
  plugin written in Java
- Device locked to Chores app only
- No browser, YouTube, or other app access
- Google Family Link as secondary parental control layer
- Google Find My Device remains active at system level
- Adaptive charging capped at 80%

### Kid Interface
- Avatar selector screen on app open (no login in v1)
- Optional profile photo per kid (fun, not security)
- Kid sees only their own chores for the current block
- Chore status per item:
  - NOT_STARTED
  - IN_PROGRESS (after first checkbox or photo attempt)
  - AWAITING_REVIEW (disputed AI rejection)
  - DONE (all shots passed or parent approved)
- Streak display visible to kid on their profile screen

### Chore Types
- **Photo verified** — kitchen chores, house rotation,
  bathroom chores, household Saturday chores, laundry
  put away (closet/dresser photo), leave no trace
  (counter/table photo)
- **Checkbox / honor system** — make bed, brush teeth,
  personal hygiene, laundry wash step

### Chore Blocks (Non-School / Summer v1)
Four blocks per day tracked independently:

| Block | Contents | Stakes |
|---|---|---|
| Daily morning | Full chore set + breakfast leave no trace | All electronics (2pm soft gate, parent tracked) |
| Lunch | Leave no trace photo (table/counter) | Electronics check, parent tracked |
| Dinner kitchen | Assigned dinner role per kid | Personal device next morning, parent flag |
| Saturday | Full Saturday set + breakfast leave no trace | Early electronics unlock as reward |

**Sunday:** Kitchen chores only, honor system,
not enforced by app.

### Chore Schedules

**Daily Morning Chores (everyone):**
- Clean your room (photo)
- Make your bed (checkbox)
- Brush teeth (checkbox)
- Personal hygiene (checkbox)
- Breakfast leave no trace — table and counter
  (photo, leave no trace)

**Daily Kitchen Rotation (1 kid assigned):**
- Clear the table (photo)
- Set the table (photo)
- Put away food (photo)
- Clean counter (photo)
- Rotation: every 12 months, manually triggered
  by parent

**Daily House Rotation — Summer/Non-School (1 kid assigned):**

Each kid is assigned one pickup category for the
summer. They are responsible for that category
across ALL common rooms (Family Room, Living Room,
Storm Room, Halls).

Assignments (rotate annually, manually triggered):
- Kid 1 — pick up trash
- Kid 2 — pick up clothes
- Kid 3 — pick up toys
- Kid 4 — pick up sporting goods
- Kid 5 — pick up shoes and school supplies

Required shots per assignment:
- 4 indoor room shots (Family/Living/Storm/Halls)
- Kid 4 adds 1 outdoor storage location shot

AI verification tier per assignment:
- Trash → AI_RELIABLE (clutter detection)
- Clothes → AI_RELIABLE (clutter detection)
- Toys → AI_WITH_OVERRIDES (clutter detection)
- Sporting goods indoor → AI_WITH_OVERRIDES
- Sporting goods outdoor storage → PARENT_QUEUE_ONLY
- Shoes/supplies → AI_WITH_OVERRIDES

Rotation: every 12 months, manually triggered
by parent in dashboard.

---

**AI Verification Tiers (applies to all chores):**

Each chore has a verification_tier field configured
in the parent dashboard:

**AI_RELIABLE:**
Chore routes through GPT-4o. AI verdict is trusted.
Kid can dispute if AI fails → parent notified.
Used for: trash, clothes, clean room, bathroom
surfaces, vacuumed floors, laundry put away.

**AI_WITH_OVERRIDES:**
Chore routes through GPT-4o. AI verdict is a first
pass but parent override rate expected to be higher.
Kid can dispute if AI fails → parent notified.
Used for: toys, shoes/supplies, sporting goods
inside, leave no trace checks.

**PARENT_QUEUE_ONLY:**
GPT-4o call skipped entirely. Submission goes
directly to AWAITING_REVIEW status. Both parents
notified immediately for manual review.
Used for: sporting goods outside, any chore
parent flags as too ambiguous for AI.

**CHECKBOX / HONOR_SYSTEM:**
No photo, no AI. Kid taps checkbox. Done.
Used for: make bed, brush teeth, personal hygiene,
laundry wash step.

**Laundry Day (1 assigned day per kid per week):**
- Wash laundry (checkbox)
- Put away laundry — closet/dresser photo required
- Carries over if missed (flagged as overdue)
- Shows up as extra daily chore on assigned day only

**Saturday Chores (everyone):**
- Clean your room (photo)
- Clean your bathroom area (checkbox)
- Brush teeth (checkbox)
- Personal hygiene (checkbox)
- Do laundry (same as laundry day logic)
- Breakfast leave no trace (photo)

**Saturday Household Rotation (1 room per kid):**
- 5 rooms: family room, living room, stairs,
  storm room, half bath
- 1 room per kid per Saturday
- Rotates monthly, app tracks automatically
- Manually triggered rotation in parent dashboard

**Saturday Bathroom Rotation (per assigned bathroom):**
- 2 bathrooms: guest bathroom, main kids bathroom
- Kids assigned to bathroom they primarily use
- 4 tasks rotating weekly, app tracks automatically:
  - Toilet cleaning
  - Mirror and upkeep (soap, towels, TP, trash,
    clothes pickup)
  - Counter and sinks
  - Tub and floor
- Weekly rotation, app tracks automatically

**Dinner Kitchen Roles (fixed, 12-month rotation):**
- Kid1 — clears and wipes table
- Kid2 — sets the table (dishes, condiments)
- Kid3 — puts away leftovers
- Kid4 — clears all non-leftover dishes
- Kid5 — wipes down counters
- Summer/break variation: 1-2 kids swap to counter
  wipe role when daily leave no trace reduces table
  cleaning need. Configured manually in parent dashboard
  at start of each break period.
- Annual rotation manually triggered by parent

### Photo Verification System
- Each chore has 1 or more required shots defined
  by parent during chore setup
- Each shot has:
  - Name / label (e.g. "Living Room - Main Area")
  - Reference photo uploaded by parent
  - AI prompt describing what to check
  - Hint text for kid (e.g. "Stand in the doorway
    facing the couch")
- Kid interface:
  - Reference photo and live camera shown side by side
  - Tap camera view to go full screen
  - Tap back to return to side by side
  - "Take Photo" button captures and submits shot
- All required shots must pass to complete chore
- If one shot fails, only that shot needs to be retaken
- Passed shots are saved — progress not lost

### AI Verification
- GPT-4o Vision API
- Two modes:
  - Reference comparison (bedroom, floors, surfaces)
  - Clutter detection (trash, clothes, shoes, toys)
- Returns PASS or FAIL with brief reason
- FAIL reason shown to kid on screen
- Kid can dispute → escalates to parent review
- No retry limit in v1 (v2 adds 3 strike limit)

### Chore Completion & Dispute Flow
```
Kid submits photo
      ↓
GPT-4o evaluates
      ↓
PASS → shot marked complete
      ↓
All shots complete → chore DONE
      
FAIL → reason shown to kid
      ↓
Kid can dispute → AWAITING_REVIEW
      ↓
Push notification to both parents
      ↓
Parent reviews photo in dashboard
      ↓
Approve → chore DONE
Reject (v2) → back to kid with note
```

### Parent Dashboard
- **Review queue** — disputed chores awaiting sign off
  - Side by side reference vs submitted photo
  - AI verdict and reason shown
  - Approve button (with optional note)
  - Reject button (v2)
- **Chore setup**
  - Create / edit chore definitions
  - Define required shots per chore
  - Upload reference photos per shot
  - Set chore type (photo / checkbox)
  - Assign to chore block (daily/saturday/dinner/laundry)
  - Set AI prompt and kid hint per shot
- **Rotation management**
  - View current rotation assignments
  - Manually trigger rotation for any rotation type
  - Assign laundry days per kid
  - Configure summer dinner role variation
  - Assign kids to bathrooms
- **History and reporting**
  - Streak per kid (consecutive days all chores complete)
  - Consistency report per chore (completion rate over
    time, helps identify problem chores)
  - Daily log — what got done, what didn't, per kid
- **Next-day device flag**
  - Clear indicator per kid showing if dinner kitchen
    chore was completed previous evening
  - Visible on dashboard home screen every morning
- **Account management**
  - Primary parent invites second parent via email
  - Both have equal full permissions
  - Kid profile setup (name, avatar/photo)
  - Bathroom assignment per kid
  - Rotation configuration

### Notifications (v1)
- Push to both parents when kid disputes AI rejection
- Notification contains kid name, chore name, deep link
  to review queue

### Streaks
- Tracked per kid
- Increments when all chores in daily block complete
- Visible to kid on their profile screen
- Visible to parent in dashboard
- Resets on missed day

---

## Version 2

### School vs Non-School Schedule
- School day chore set (lighter — no house rotation,
  add put away bags and coats etc.)
- Non-school day chore set (current v1 summer mode)
- Holiday / semester break detection
- School calendar integration or manual calendar
  in parent dashboard

### Face Login Check-in
- ML Kit on-device face recognition
- Fun check-in moment, not strict security
- 3-5 reference photos per kid taken during setup
- Fallback to avatar tap if face not recognized
- False positives acceptable

### Chore Instructions and Supply List
- Each chore has optional step-by-step instructions
- Supply list per chore (cleaning products, tools)
- Visible to kid before starting chore
- Parent configures in dashboard
- GPT can auto-generate suggestions when creating
  a new chore

### Parent Rejection with Notes
- Parent can reject disputed chore with written note
- Note pushed to kid on chore device
- Kid must resubmit
- 3 strike limit per chore per day to prevent spam

### Grounding Tracker
- Parent sets grounding for specific kid
- End date and time recorded precisely
- Visible to kid on their profile screen
- Visible to parent on dashboard
- Eliminates "I thought it ended Saturday" disputes
- Push notification to parent when grounding expires

### Chore Deadline Notifications
- Configurable reminder times per chore block
- Push to parent if block incomplete past deadline

---

## Version 3

### Wall Tablet Home Hub
- Samsung Galaxy A9+ 10.9" wall mounted
- Always-on ambient family display
- Family calendar
- Shopping list (shared, anyone can add)
- Family message board / words of affirmation
- Chore status overview for all kids
- Weather widget
- Meal planner

### EXIF Timestamp Validation
- Verify photo metadata confirms taken same day
- Prevent submitting old photos
- metadata-extractor Java library

### Dock Alarm System
- Bluetooth beacon on charging dock
- USB-C connection state detection
- Timer starts when device leaves dock
- Tiered alerts:
  - 5 min → on-screen reminder
  - 10 min → loud device alarm
  - 10 min → push to parent phone
  - 15 min → device locks until docked
- Heartbeat to Spring Boot backend
- FCM alert to parent independent of device

### Common Room Cameras
- Wyze cam integration (~$35 per room)
- Living room, family room, kitchen, storm room
- Triggered snapshot on chore submission
- Wyze API integrated with Spring Boot backend
- Eliminates need for device in common rooms

### Profile Management
- Add / remove / modify kid profiles
- Partial year profiles (e.g. college kid home
  for summer only)
- Age-based chore exceptions for younger kids
- Younger kid specific rotation assignments
  (toy area, help take out trash, etc.)

### MDM Integration — Headwind MDM
- Self-hosted on Spring Boot backend
- Device Owner policy management
- Remote wipe / lock capability
- Call and text lockdown to approved contacts only
- WiFi data restricted to Chores app only
- Approved contact list managed from parent dashboard
- Replaces Alexa calling for kids

### Wyze Camera Integration
- Spring Boot → Wyze API
- Auto-snapshot on chore submission for common rooms
- No device needed in hand for common room chores