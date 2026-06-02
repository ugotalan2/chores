# DECISIONS.md

## Purpose
A running log of key architectural, product, and hardware
decisions made during the design of Chores, including
the reasoning behind each. Update this file whenever a
significant decision is made or changed. This file exists
so that months later we can remember WHY we did something,
not just WHAT we did.

---

## Architecture Decisions

### AD-001 — Spring Boot as Backend Framework
**Decision:** Use Spring Boot (Java) as the backend API.
**Alternatives considered:** Node.js/Express, Python/Django
**Reason:** Primary learning goal of this project is Java.
Spring Boot is industry standard, has excellent docs and
community support, and Java is consistent across both the
backend and the native Android kiosk plugin code written
for the chore device. Killing two birds with one stone
on the learning goal.
**Date:** Project inception
**Status:** Final

---

### AD-002 — Firebase over PostgreSQL
**Decision:** Use Firebase (Firestore, Auth, Storage, FCM)
as the primary data and services platform.
**Alternatives considered:** PostgreSQL (Supabase/Neon),
MySQL (PlanetScale), MongoDB Atlas
**Reason:** Firebase provides database, authentication,
file storage, and push notifications all in one free
platform that works together natively. For a project
this size the free tier is more than sufficient.
Firestore's document model fits the chore/schedule data
structure well. FCM is needed for push notifications
anyway so keeping everything in one ecosystem reduces
complexity significantly.
**Date:** Project inception
**Status:** Final

---

### AD-003 — Angular + Ionic + Capacitor Frontend
**Decision:** Build frontend in Angular with Ionic UI
components, wrapped in Capacitor for Android.
**Alternatives considered:** React Native, Flutter,
pure PWA
**Reason:** Angular was the chosen frontend framework.
Ionic is purpose-built for Angular-to-mobile and provides
native-feeling UI components out of the box. Capacitor
wraps the Angular app into a real Android APK with access
to native device APIs (camera, charging state, BLE).
One codebase serves both the Android kiosk app and the
parent PWA with minimal duplication. Capacitor native
plugins are written in Java, consistent with the backend
learning goal.
**Date:** Project inception
**Status:** Final

---

### AD-004 — PWA for Parent Dashboard
**Decision:** Parent dashboard delivered as a PWA, not
a native iOS app.
**Alternatives considered:** Native iOS app (Swift),
React Native cross-platform app
**Reason:** Apple Developer account costs $99/year and
requires App Store review for distribution. This is a
personal family app — that overhead is unnecessary. A PWA
added to the iPhone home screen via Safari is functionally
identical for this use case. Zero cost, zero gatekeeping,
same Angular codebase as the Android app with shared
components. Works on any device with a browser.
**Date:** Project inception
**Status:** Final

---

### AD-005 — GPT-4o Vision API for Photo Verification
**Decision:** Use GPT-4o Vision API for AI chore
photo verification.
**Alternatives considered:** AWS Rekognition,
Google Vision API, Azure Computer Vision, custom
trained model
**Reason:** GPT-4o handles both verification modes
(reference comparison AND object/clutter detection)
in a single API with no custom model training. Context-
aware prompting per chore type makes it flexible enough
to handle "does this match the clean reference photo"
AND "is there still trash on the floor" without separate
systems. Cost is pay-per-use (cents per photo) which is
acceptable for a family app with low volume.
**Date:** Project inception
**Status:** Final

---

### AD-006 — Two AI Verification Modes
**Decision:** Implement two distinct AI verification
modes — reference comparison and clutter detection.
**Reason:** Not all chores can be verified the same way.
A vacuumed floor or made bed is best verified by comparing
to a known clean reference photo. Pickup chores (trash,
clothes, shoes, toys) are better verified by asking the
AI to detect the presence of specific objects. Using
the right mode per chore type improves accuracy and
reduces false failures.
**Reference comparison used for:** bedroom, vacuumed
floors, bathroom surfaces, kitchen counters
**Clutter detection used for:** trash pickup, laundry
pickup, shoes, toys, sporting goods
**Date:** Project inception
**Status:** Final

---

### AD-007 — Multi-Shot Verification Per Chore
**Decision:** Each chore has a defined set of required
shots that ALL must pass before the chore is complete.
**Reason:** A single photo can't capture a whole room
and creates too many opportunities for kids to game
the system (photographing the clean corner, hiding
mess out of frame). Multiple required shots from
defined angles close this loophole. Parent defines
shots during chore setup with reference photos and
hint text guiding the kid to the right angle.
**Date:** Project inception
**Status:** Final

---

### AD-008 — Kid Can Dispute AI Rejection
**Decision:** If AI fails a photo, kid can dispute it
and escalate to parent review. No retry limit in v1.
**Reason:** AI will occasionally fail legitimate clean
rooms. Kids need a recourse that doesn't require
parents to be physically present. Escalation to parent
review queue keeps parents in control while not
blocking kids unfairly. Retry limit intentionally
deferred to v2 — v1 establishes baseline behavior
before adding restriction logic.
**Date:** Project inception
**Status:** Final — v2 adds 3 strike limit

---

### AD-009 — No Offline Mode
**Decision:** App requires internet connection. No
offline mode.
**Reason:** AI photo verification requires GPT-4o API
call which needs internet. EXIF validation (v3) also
server-side. App is used at home on home WiFi — offline
scenario is not a realistic use case. Adding offline
complexity would significantly increase build time
for no practical benefit.
**Date:** Project inception
**Status:** Final

---

### AD-010 — Role-Based Access Control
**Decision:** Two roles — ROLE_PARENT and ROLE_CHILD.
All API endpoints secured by role. Kids cannot access
any parent features even if they navigate to the PWA URL.
**Reason:** Security model must prevent kids from
changing their own chore assignments, approving their
own submissions, or modifying any settings. Role
enforcement at the Spring Boot API level ensures this
regardless of which interface is used to access the
backend. The Android app simply has no routes to parent
features as an additional layer.
**Date:** Project inception
**Status:** Final

---

## Product Decisions

### PD-001 — Shared Single Chore Device
**Decision:** All 5 kids share one chore device.
**Alternatives considered:** One device per kid,
two devices (upstairs/downstairs)
**Reason:** Kids are not allowed devices until chores
are done — a shared dedicated device solves the
chicken-and-egg problem. One device is simpler to
manage, cheaper, and sufficient for v1. If handoff
becomes a bottleneck a second device can be added
later with no code changes needed.
**Date:** Project inception
**Status:** Final — revisit if handoff causes friction

---

### PD-002 — No Login for Kids in v1
**Decision:** Kids identify themselves by tapping their
avatar on a profile selector screen. No password or PIN
in v1.
**Reason:** Simplest possible kid UX for v1. The goal
in v1 is to get the core chore verification working.
Face login (v2) will replace this with something more
engaging. Security risk is low — worst case a sibling
submits chores for another sibling, which is acceptable
per the household rules (chore must be done, doesn't
matter who helped).
**Date:** Project inception
**Status:** Final — v2 replaces with ML Kit face login

---

### PD-003 — App Does Not Enforce Screen Time
**Decision:** Chores tracks chore completion and
flags status but does NOT lock kids' personal devices
or enforce screen time rules.
**Reason:** Kids' personal devices (phones, iPads,
Switch, Xbox) are separate from the chore device.
Physical enforcement (locked office door for device
collection) is already working. The app's job is to
be the parent's memory aid and verification system,
not a technical enforcement mechanism. Adding device
enforcement adds significant complexity for a problem
already solved by physical means.
**Date:** Project inception
**Status:** Final

---

### PD-004 — Dinner Kitchen Chore Tied to Next-Day
Personal Device Privilege
**Decision:** After-dinner kitchen chore completion (or
lack thereof) is tracked separately and displayed as a
flag on the parent dashboard the following morning.
**Reason:** Kids already have electronics by dinner time
so the dinner chore can't gate same-day screen time.
Instead completion determines whether their personal
device is returned the next morning. Parent checks
dashboard flag in the morning rather than trying to
remember from the night before. App is the memory aid,
parent makes the enforcement call.
**Date:** Project inception
**Status:** Final

---

### PD-005 — Chore Doesn't Have to Stay Clean
**Decision:** A chore is complete if the photo passes
at any point during the day. Room does not need to
remain clean.
**Reason:** Realistic household rule. Enforcing
cleanliness all day is unmanageable with 5 kids.
The goal is establishing the habit of cleaning, not
perfection. EXIF timestamp (v3) will confirm photo
was taken same day.
**Date:** Project inception
**Status:** Final

---

### PD-006 — Laundry Day Assigned Per Kid
**Decision:** Each kid has an assigned day of the week
for laundry. Shows up as an extra daily chore that day
only. Carries over if missed (flagged as overdue).
**Reason:** Without assigned days all 5 kids attempt
laundry Saturday morning causing conflicts over
machines. Spreading across the week eliminates the
bottleneck. Carryover prevents kids from simply
skipping their laundry day with no consequence.
**Date:** Project inception
**Status:** Final

---

### PD-007 — Laundry Verified by Closet/Dresser Photo
**Decision:** Laundry put-away step requires a photo
of kid's closet or dresser showing clothes organized.
Wash step is checkbox/honor system.
**Reason:** The behavioral problem to solve is clothes
living permanently in the laundry basket (can't
distinguish clean from dirty, over-washing). A photo
of the washer/dryer doesn't prove put-away happened.
A photo of an organized closet/dresser does.
Wash step is honor system — machine takes 2 hours,
hard to fake, low value to verify.
**Date:** Project inception
**Status:** Final

---

### PD-008 — Bathroom Rotation Weekly by Assigned Bathroom
**Decision:** Each kid is assigned to the bathroom they
primarily use. 4 tasks (toilet, mirror & upkeep, counter
& sinks, tub & floor) rotate weekly within that bathroom.
App tracks rotation automatically.
**Reason:** With 5 kids across 2 bathrooms (2 in one,
3 in the other) a shared rotation across both bathrooms
leaves some tasks uncovered. Assigning by bathroom
ensures every part of every bathroom gets cleaned
weekly. Weekly rotation was previously too hard to
track manually — the app removes that friction entirely.
**Date:** Project inception
**Status:** Final

---

### PD-009 — Saturday is Always Big Chore Day
**Decision:** Saturday chore schedule is the same
year-round regardless of school/break status.
**Reason:** Saturday is consistently the household's
deep clean day. School vs non-school distinction
(v2) only affects weekday schedules. Saturday routine
is stable and well established — no need to vary it
by season.
**Date:** Project inception
**Status:** Final

---

### PD-010 — Streaks as Primary Gamification in v1
**Decision:** Track consecutive days of full chore
completion per kid as the primary engagement mechanic.
Visible to both kids and parents.
**Reason:** Streaks are simple to understand at all
ages (7 through 16), self-motivating, and require
zero additional UI complexity. Parents benefit from
streak data as a consistency signal — a kid with a
broken streak is worth checking in on. More complex
reward systems deferred to future versions.
**Date:** Project inception
**Status:** Final

---

### PD-011 — Grounding Tracker Deferred to v2
**Decision:** Grounding tracker (exact end date/time
visible to parents and kids) is a v2 feature.
**Reason:** Not core to chore verification MVP. However
the problem is real — kids consistently misremember
grounding end dates. Adding this in v2 alongside
school schedule logic makes sense as both are
schedule/calendar related features.
**Date:** Project inception
**Status:** Deferred to v2

---

## Hardware Decisions

### HD-001 — Unlocked Phone over Carrier-Locked
**Decision:** Purchase unlocked Samsung A14 ($89) with
free TextNow SIM over carrier-locked phones with
monthly plans.
**Reason:** Device never leaves the house. Cellular
data is never needed — all traffic runs over home WiFi.
Any carrier plan (even $15/month) costs more than
the hardware price within 6 months. Unlocked + TextNow
= $0/month forever. Break-even vs cheapest carrier
plan is under 3 months.
**Date:** Project inception
**Status:** Final

---

### HD-002 — Phone over Tablet for Chore Device
**Decision:** Use a phone (not a tablet) as the shared
chore submission device.
**Reason:** Kids need to carry the device into bedrooms
and bathrooms for photo verification. A phone is
significantly easier to carry one-handed, especially
for the 7 year old. Tablets are awkward to carry and
more likely to be dropped. Phone is also cheaper to
replace if damaged.
**Date:** Project inception
**Status:** Final

---

### HD-003 — No Fixed Cameras in Bedrooms or Bathrooms
**Decision:** Wyze camera integration (v3) covers common
rooms only. Bedrooms and bathrooms explicitly excluded.
**Reason:** Privacy. Non-negotiable. Kids carry the
shared chore device into private spaces for photos.
Fixed cameras in private spaces are not acceptable
regardless of technical convenience.
**Date:** Project inception
**Status:** Final — non-negotiable

---

### HD-004 — Adaptive Charging Capped at 80%
**Decision:** Enable Android adaptive charging and cap
battery at 80% on the chore device.
**Reason:** Device is plugged in most of the day between
chore sessions. Keeping lithium battery constantly at
100% accelerates degradation. 80% cap significantly
extends battery lifespan. At $89 replacement cost this
is a nice-to-have rather than critical, but easy to
implement and worth doing.
**Date:** Project inception
**Status:** Final

---

### HD-005 — Wall Tablet Deferred to v3
**Decision:** Wall-mounted home hub tablet is a v3
feature. V1 and v2 use existing parent devices for
dashboard access.
**Reason:** Wall hub is an expansion of the app's
vision beyond chore verification. Building the core
chore system first (v1) and parent dashboard as PWA
(works on existing phones) keeps hardware costs at
minimum for MVP. Wall hub adds ~$516 in hardware
and significant new feature scope — appropriate
after core system is proven and stable.
**Date:** Project inception
**Status:** Deferred to v3

---

## Rotation Schedule Summary

| Rotation | Frequency | Trigger |
|---|---|---|
| Kitchen assignment | Every 12 months | Manual — parent dashboard |
| House assignment | Every 12 months | Manual — parent dashboard |
| Dinner kitchen roles | Every 12 months | Manual — parent dashboard |
| Saturday household room | Monthly | Manual trigger, app tracks |
| Saturday bathroom tasks | Weekly | Automatic, app tracks |
| Laundry day assignment | Fixed (set once) | Manual — parent dashboard |
| Summer dinner variation | Per break period | Manual — parent dashboard |

### AD-011 — PostgreSQL over Firestore
**Decision:** Use PostgreSQL (hosted on Railway) instead
of Firebase Firestore as the primary database.
**Alternatives considered:** Firebase Firestore,
MongoDB Atlas, Supabase
**Reason:** Chores data is fundamentally relational.
Kids have assignments, assignments reference chores,
chores have required shots, daily instances reference
assignments and chores. SQL handles this naturally with
foreign keys, joins, and constraints. Firestore would
require data duplication or multiple round trips to
simulate joins. Complex queries like streak calculation,
consistency reporting, and cross-kid status views are
straightforward SQL but painful in Firestore.
PostgreSQL is also more transferable as a skill and
matches what most enterprise backend teams use.
**Date:** Project design phase
**Status:** Final

---

### AD-012 — Railway over Supabase for Database
**Decision:** Host PostgreSQL and Spring Boot API on
Railway rather than Supabase.
**Alternatives considered:** Supabase (PostgreSQL),
Neon, PlanetScale
**Reason:** Existing Supabase account already has 2
active projects which is the free tier maximum. Rather
than managing a separate database hosting account,
Railway hosts both the Spring Boot API and PostgreSQL
database in one free tier under one dashboard. Simpler
to manage, no inactivity pausing issues (Supabase pauses
free projects after 7 days of inactivity which would
disrupt development), and one less account to maintain.
**Date:** Project design phase
**Status:** Final

---

### AD-013 — Clerk for OAuth Authentication
**Decision:** Use Clerk for parent OAuth authentication
on the PWA dashboard.
**Alternatives considered:** Firebase Auth, Supabase
Auth, Auth0, rolling our own
**Reason:** Clerk provides OAuth (Google sign-in) out
of the box with a generous free tier. Angular and
Spring Boot SDKs are available. Built-in invitation
flow handles adding a second parent cleanly. JWT
validation integrates with Spring Security via JWKS
endpoint. Eliminates building and maintaining auth
infrastructure entirely.
**Parent PWA:** Full Clerk OAuth login
**Chore device:** One-time Clerk setup, then
long-lived device token stored in Android Keystore.
Kids never interact with auth after initial setup.
**Date:** Project design phase
**Status:** Final

### AD-014 — Tiered AI Verification by Chore Type
**Decision:** Not all chores use AI verification.
Verification method is assigned per chore based on
how reliably AI can assess that specific task.
**Three tiers:**
- ✅ AI verification — clear visual pass/fail, AI
  handles reliably, reduces parent review load
- ⚠️ AI with expected overrides — AI attempts
  verification but parent override rate will be
  higher, still worth running to catch obvious fails
- ❌ Parent queue directly — AI unreliable for this
  chore type, skip AI and route straight to parent
  review queue

**Reasoning by chore type:**

| Chore | Tier | Reason |
|---|---|---|
| Clean room | ✅ AI reference comparison | Clear visual baseline |
| Vacuum / floors | ✅ AI reference comparison | Clear visual baseline |
| Bathroom surfaces | ✅ AI reference comparison | Clear visual standard |
| Laundry put away (closet) | ✅ AI reference comparison | Organized vs messy is clear |
| Pick up trash | ✅ AI clutter detection | Trash is visually distinct |
| Pick up clothes | ✅ AI clutter detection | Clothes on floor are obvious |
| Pick up toys | ⚠️ AI clutter detection | Broad category, edge cases |
| Pick up shoes/supplies | ⚠️ AI clutter detection | Small items, moderate reliability |
| Sporting goods (inside) | ⚠️ AI clutter detection | Reasonable but edge cases |
| Sporting goods (outside) | ❌ Parent queue directly | Outside lighting/background too variable for reliable AI |
| Checkbox chores | N/A honor system | Teeth, bed, hygiene — no photo |

**Outside photo exception:**
Rather than photographing the whole yard for
sporting goods, Kid 4 photographs the specific
storage location (equipment bin, garage wall mount,
etc.). Reference photo of full organized storage vs
empty is a reliable AI check. Shifts verification
from "is yard clean" to "is equipment put away."

**Implementation:**
Each chore in the parent dashboard has a
verification_tier field:
- AI_RELIABLE
- AI_WITH_OVERRIDES
- PARENT_QUEUE_ONLY

PARENT_QUEUE_ONLY chores skip the GPT-4o call
entirely and go straight to AWAITING_REVIEW status,
pushing notification to parents immediately.

**Date:** Project design phase
**Status:** Final

---

### AD-015 — Summer House Rotation Chore Structure
**Decision:** Summer house rotation assigns one
specific pickup category per kid across all common
rooms (Family/Living/Storm/Halls). Each kid owns
their category for the full summer break and rotates
annually with other assignments.

**Summer assignments:**
- Kid 1 — pick up trash
- Kid 2 — pick up clothes
- Kid 3 — pick up toys
- Kid 4 — pick up sporting goods (inside + outside
  storage location)
- Kid 5 — pick up shoes and school supplies

**Why category-based vs room-based:**
Room-based rotation (Kid 1 cleans the whole family
room) requires a kid to pick up everything which
creates overlap and arguments. Category-based means
each kid has one clear responsibility across all
rooms — no ambiguity about whose job it is.

**Photo requirements per kid:**
Each category requires 4 shots minimum (one per
room: Family/Living/Storm/Halls) plus Kid 4 adds
the outdoor storage location shot.

**Verification tiers assigned:**
- Kid 1 trash → AI_RELIABLE
- Kid 2 clothes → AI_RELIABLE
- Kid 3 toys → AI_WITH_OVERRIDES
- Kid 4 sporting goods inside → AI_WITH_OVERRIDES
- Kid 4 sporting goods outside → PARENT_QUEUE_ONLY
  (storage location shot only)
- Kid 5 shoes/supplies → AI_WITH_OVERRIDES

**Date:** Project design phase
**Status:** Final