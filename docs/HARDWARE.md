# HARDWARE.md

## Version 1 Hardware

### Chore Device — Samsung Galaxy A14
**Purpose:** Shared chore submission device for all 5 kids.
Kids carry it to their room or bathroom to take chore
photos, then return it to the charging dock.

**Device:**
- Samsung Galaxy A14 5G
- Factory unlocked (not carrier locked)
- 64GB storage, 13MP camera
- Standard Android (full kiosk mode support)
- Purchase: Amazon ~$89

**Carrier / Plan:**
- TextNow free SIM
- WiFi calling and texting only
- $0/month — device never leaves the house
- No cellular data needed, all app traffic over home WiFi

**Kiosk Lockdown:**
- Android Device Owner mode
- Locked to Chores app only
- No browser, YouTube, or app installs accessible
- Google Family Link as secondary control layer
- Google Find My Device active at system level
- Adaptive charging capped at 80% to preserve
  battery health long term

**Charging Dock:**
- Small countertop charging stand
- 90-degree USB-C cable to reduce cable wear
- Located in main hallway or kitchen counter
- Cable anchor/zip tie to wall so cable stays put

**Case:**
- Rugged case with hand strap
- Hand strap critical — reduces drops during
  carry to bedroom/bathroom
- Shockproof silicone, easy to clean
- All ports accessible

**Battery Management:**
- Adaptive charging enabled, cap at 80%
- Smart plug on charger to schedule charging
  windows if needed (~$10)
- At $89 replacement cost, battery degradation
  over 3+ years is acceptable risk

**Quantity:** 1 device to start. Add second if
handoff between kids becomes a bottleneck.

---

### Parent Devices
**No dedicated hardware needed for v1.**

Parent dashboard is a PWA accessed via Safari on
existing iPhone or any browser on any device.
- Add to home screen in Safari for native app feel
- No App Store, no Apple Developer account needed
- Both parents access via their own existing devices

---

## Version 2 Hardware

### Phone as Family Counter Phone
**Purpose:** Upgrade chore device to also serve as
a locked-down family counter phone, replacing Alexa
calling for kids.

**Additional Configuration:**
- Headwind MDM installed (self-hosted, free)
- Call and text enabled to approved contacts only
- WiFi data restricted to Chores app only
- No browsers, social, or general internet access
- Approved contact list managed from parent dashboard
- Essentially a modern smart landline for the house

**Approved Contacts (example):**
- Mom
- Dad
- Grandma
- Grandpa
- Emergency contacts

**MDM Solution:** Headwind MDM
- Open source, self-hosted on Spring Boot backend
- Free
- Full Device Owner policy control
- Survives factory reset attempts
- REST API integrates with Spring Boot parent dashboard

---

## Version 3 Hardware

### Wall Hub Tablet — Samsung Galaxy A9+
**Purpose:** Always-on wall-mounted family home hub
display. Permanent fixture, never removed from wall.
Runs PWA in ambient/kiosk mode showing family
calendar, chore status, shopping list, messages,
weather, and more.

**Device:**
- Samsung Galaxy A9+ 10.9"
- Full Android, standard Google Play
- 10.9" screen — large enough for ambient wall display
- Newer chip with several years of Android updates
- Purchase: ~$188-220

**Why bigger than chore phone:**
- 8.7" feels small as a permanent wall display
- Family glances at it throughout the day
- Calendar, shopping list, messages need readable
  screen real estate
- 10.9" is the sweet spot between cost and usability

**Wall Mount:**
- Dockem Koala wall mount (~$25)
- Adjustable rails accommodate case thickness
- Screws into wall securely
- Tablet slides in/out for occasional removal
- Charging cable feeds through mount

**Case:**
- Magsafe-compatible ring case
- Allows magnetic attachment to mount
- Protects during any removal
- Kids shouldn't be removing this device

**Charging:**
- 90-degree USB-C cable
- Cable routed through mount and zip tied
- Recessed in-wall outlet behind mount
  for clean installation (optional upgrade)

**Quantity:** 2 stations recommended
- Main floor (kitchen/hallway area)
- Upstairs hallway

**Per Station Cost:**
| Item | Cost |
|---|---|
| Samsung A9+ | ~$200 |
| Dockem Koala mount | ~$25 |
| Magsafe ring case | ~$15 |
| 90-degree USB-C cable | ~$10 |
| Screen protector | ~$8 |
| **Total per station** | **~$258** |

**Two stations total:** ~$516

---

### Bluetooth Beacon — Dock Detection
**Purpose:** Detect when chore phone has left the
charging dock without relying solely on USB connection
state. Enables dock alarm system.

**Device:** Generic iBeacon or Estimote beacon
- Mounts on or near charging dock
- Coin battery lasts years
- BLE signal detected by chore phone continuously
- ~$15-20 per beacon

**How it works:**
- Phone continuously scans for beacon BLE signal
- Strong signal = docked
- Signal lost = phone has left dock area
- Combined with USB-C disconnect detection for
  double confirmation
- Triggers dock alarm timer in app

---

### Common Room Cameras — Wyze Cam
**Purpose:** Eliminate need for chore device in common
rooms. Camera takes auto-snapshot when kid submits
a common room chore, AI verifies the room directly.

**Device:** Wyze Cam v3 or v4
- ~$35 per camera
- WiFi connected
- Wyze API available for Spring Boot integration
- Auto-snapshot triggered by backend on submission

**Placement:**
- Living room
- Family room
- Kitchen
- Storm room
- (Bedrooms and bathrooms explicitly excluded
  for privacy)

**Per Camera Cost:** ~$35
**4 cameras total:** ~$140

---

## Hardware Summary by Version

| Version | Hardware Added | Est. Cost |
|---|---|---|
| v1 | Samsung A14 + case + dock | ~$120 |
| v2 | MDM config only, no new hardware | $0 |
| v3 | 2x A9+ wall tablets + mounts + 4x Wyze cams + beacons | ~$700 |

**Total hardware investment across all versions:** ~$820

---

## Hardware Decisions Log

**Why unlocked phone over carrier-locked:**
Carrier-locked phones require a monthly plan even for
basic talk/text. At $89 unlocked + $0/month TextNow,
break-even vs any carrier plan is under 3 months.
Long-term cost is significantly lower.

**Why separate phone vs tablet for chore device:**
Phone is easier for kids to carry one-handed to bedrooms
and bathrooms. Tablets are awkward to carry and harder
for younger kids (age 7) to manage. Phone also cheaper
to replace if damaged.

**Why not use kids' existing devices:**
Kids are not allowed devices until chores are complete.
A dedicated shared device solves this chicken-and-egg
problem cleanly. No exceptions, no negotiation.

**Why not fixed cameras in bedrooms/bathrooms:**
Privacy. Hard no. Fixed cameras only appropriate for
common living areas.

**Why Wyze over other cameras:**
Price (~$35), reliability, and available API for
Spring Boot integration. No proprietary lock-in.

**Why PWA for parent dashboard vs native iOS:**
Apple Developer account costs $99/year. App Store
review adds friction for personal family app. PWA
added to home screen via Safari is functionally
identical for this use case. Zero cost, zero gatekeeping.