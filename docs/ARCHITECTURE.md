# ARCHITECTURE.md

## System Overview
ChoreHouse consists of four main components:
1. **Spring Boot Backend** — Java REST API, business logic,
   AI orchestration
2. **Firebase Platform** — database, authentication, push
   notifications, file storage
3. **Android App** — shared chore device, kid-facing,
   locked down via kiosk mode
4. **Parent PWA** — parent-facing dashboard, runs in
   Safari/Chrome on any device, no app store needed

---

## Backend — Spring Boot (Java)

**Why Spring Boot:**
- Primary learning goal of the project is Java
- Industry standard, large community, excellent docs
- Native Android kiosk plugin pieces also in Java
- Consistent language across backend and device-level code

**Responsibilities:**
- REST API for all app/dashboard interactions
- Chore schedule logic (day type, rotation tracking)
- AI verification orchestration (calls GPT-4o Vision API)
- Rotation management (bathroom weekly, household monthly,
  kitchen/house/dinner annually)
- Chore block logic (daily, breakfast, lunch, dinner)
- Streak calculation and consistency reporting
- Push notification triggers via Firebase Cloud Messaging
- Role-based access control (PARENT vs CHILD)
- Missed chore carry-over logic (laundry)
- Next-day personal device flag logic

**Key Libraries:**
- Spring Web — REST endpoints
- Spring Security — JWT auth, role enforcement
- Spring Data JPA — database ORM
- metadata-extractor — EXIF photo data (v3)
- Firebase Admin SDK — push notifications
- OpenAI Java SDK — GPT-4o Vision API calls

---

## Database — Firebase Firestore

**Why Firebase over PostgreSQL:**
- Free tier covers this use case comfortably
- Firestore document model fits chore/schedule data well
- Built-in real-time updates (parent dashboard live refresh)
- Firebase Auth handles both parent and kid sessions
- Firebase Storage handles reference + submission photos
- Firebase Cloud Messaging handles push notifications
- One platform for auth, db, storage, and notifications

**Collections overview (detail in DATA_MODEL.md):**
- `users` — parent and kid profiles
- `chores` — chore definitions with required shots
- `choreAssignments` — which kid has which chore/rotation
- `choreBlocks` — daily/breakfast/lunch/dinner block config
- `submissions` — photo submissions and AI verdicts
- `rotations` — rotation schedules and current state
- `streaks` — per kid streak tracking
- `notifications` — push notification log

---

## Android App — Angular + Ionic + Capacitor

**Why this stack:**
- Angular is the chosen frontend framework
- Ionic provides mobile-native UI components out of the box
- Capacitor wraps Angular into a real Android APK
- One codebase shared with PWA, minimal duplication
- Capacitor supports native Android plugins written in Java

**Kid-facing features:**
- Avatar selector screen (no login in v1)
- Daily chore list based on current day type and block
- Reference photo + live camera side by side view
- Full screen camera option via tap
- Checkbox completion for honor system chores
- Dispute button on AI rejection → escalates to parent
- Chore status indicators (not started/in progress/
  awaiting review/done)
- Streak display per kid

**Android Kiosk Lockdown:**
- Android Device Owner mode via custom Capacitor plugin
  written in Java
- Locks device to ChoreHouse app only
- No browser, no YouTube, no app installs
- Google Family Link for additional parental controls
- Find My Device remains active at system level
- Adaptive charging capped at 80% to preserve battery

**Bluetooth Beacon Integration (v3 dock detection):**
- iBeacon or Estimote beacon mounted on charging dock
- Capacitor plugin detects beacon proximity
- Combined with USB-C connection state detection
- Triggers dock alarm if away too long

---

## Parent PWA — Angular + Ionic

**Why PWA instead of native iOS app:**
- No Apple Developer account needed ($99/year saved)
- No App Store review process
- Add to home screen on iPhone via Safari
- Works full screen like a native app
- Same Angular codebase as Android app, shared components
- Protected routes only accessible with PARENT role JWT

**Parent-facing features:**
- Awaiting review queue with side-by-side photo comparison
- Approve/reject with optional note
- Push notifications for disputed chores
- Chore setup — create chores, define required shots,
  upload reference photos
- Rotation management — manual trigger for all rotations
- Kid profile management
- Streak and consistency reporting dashboard
- Dinner role configuration including summer variation
- Laundry day assignment per kid
- Next-day personal device flag view
- Second parent invite with full equal permissions

---

## AI Verification — GPT-4o Vision API

**Why GPT-4o:**
- Handles both verification modes in one API
- No custom model training needed
- Context-aware prompting per chore type
- Can reason about specific objects (trash, clothes, shoes)
- Can compare submitted photo against reference photo

**Two verification modes:**

**Mode 1 — Reference Comparison**
Used for: bedroom, vacuumed floors, bathroom surfaces
Prompt includes reference photo + submitted photo
AI determines if submitted matches cleanliness standard
of reference

**Mode 2 — Object/Clutter Detection**
Used for: trash pickup, laundry pickup, shoes, toys
Prompt describes what to look for
AI determines pass/fail based on presence of clutter
Example prompt: "This chore is 'pick up trash'. Does this
room contain visible trash, clothes, shoes, or toys on
the floor or furniture? Return PASS if clean, FAIL with
brief reason if not."

**Verification flow:**
1. Kid submits photo
2. Spring Boot receives photo, stores in Firebase Storage
3. Spring Boot calls GPT-4o with photo + chore prompt
4. GPT-4o returns PASS/FAIL + reason
5. If PASS → shot marked AI_PASSED
6. If FAIL → kid sees reason, option to dispute
7. If dispute → status AWAITING_REVIEW, parent notified
8. All shots AI_PASSED or PARENT_OVERRIDE → chore DONE

---

## Push Notifications — Firebase Cloud Messaging (FCM)

**Triggers:**
- Kid disputes AI rejection → both parents notified
- Chore block deadline approaching (v2)
- Grounding end date reminder (v2)

**Parent notification contains:**
- Kid name
- Chore name
- Shot that failed
- Deep link to review queue in PWA

---

## Hardware Integration

**Chore Device:**
- Samsung Galaxy A14 (unlocked, $89)
- Runs Android app in kiosk mode
- Shared by all 5 kids
- Charging dock with Bluetooth beacon (v3)
- Google Find My Device active at system level
- Adaptive charging at 80%

**Wall Hub Tablet (v3):**
- Samsung Galaxy A9+ 10.9"
- Magsafe ring + wall mount
- Always-on family home hub display
- Runs PWA in ambient mode

**Common Room Cameras (v3):**
- Wyze cams (~$35 each)
- Living room, family room, kitchen, storm room
- Triggered snapshot on chore submission
- Wyze API integration with Spring Boot

---

## Security Model

**Two roles:**
- `ROLE_PARENT` — full access, all endpoints
- `ROLE_CHILD` — read own chore list, submit photos only

**Enforcement:**
- JWT tokens issued by Firebase Auth
- Spring Security validates role on every endpoint
- Android app has no routes to parent features
- Parent PWA routes protected by role guard
- Kids cannot modify chore definitions, rotations,
  or any settings

---

## Hosting

| Component | Platform | Cost |
|---|---|---|
| Spring Boot API | Railway or Render free tier | Free |
| Firebase (db/auth/storage/fcm) | Firebase free tier | Free |
| Angular PWA | Firebase Hosting | Free |
| GPT-4o Vision API | OpenAI pay per use | ~cents per photo |
| Android APK | Sideloaded directly | Free |