# Chores

A family chore management app that uses AI-powered photo
verification to confirm chores are actually completed —
no more "I did it I promise."

Built as a Java/Spring Boot learning project designed
to practice enterprise development workflows including
CI/CD pipelines, Docker, database migrations, and
test-driven development.

---

## The Problem
6 kids. Constant chore arguments. Parents can't always
physically verify every chore. Kids submit "done" when
they aren't. Hard to track who did what and when.

## The Solution
A dedicated locked-down Android device shared by all
kids. Kids select their avatar, complete their chores,
and submit photos for AI verification. Parents get a
PWA dashboard to review, approve, and manage everything.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 3.x |
| Database | PostgreSQL (Railway) |
| Migrations | Flyway |
| Auth | Clerk (OAuth) |
| Frontend | Angular + Ionic |
| Mobile | Capacitor (Android) |
| AI Verification | GPT-4o Vision API |
| Push Notifications | Firebase Cloud Messaging |
| File Storage | Supabase Storage |
| Hosting | Railway (API + DB), Firebase (PWA) |
| CI/CD | GitHub Actions |
| Containers | Docker |

---

## Project Documentation

All design decisions, architecture, data model, and
feature planning documented in `/docs`:

| File | Contents |
|---|---|
| PROJECT_OVERVIEW.md | What, why, who, rules |
| ARCHITECTURE.md | How all pieces connect |
| FEATURES.md | V1/V2/V3 feature breakdown |
| HARDWARE.md | Devices, mounts, costs |
| DATA_MODEL.md | Full database schema |
| DECISIONS.md | Why we made every key decision |
| CICD.md | Pipeline, branching, Docker setup |
| GOTCHAS.md | Known pitfalls and fixes |

**Start here when resuming development in a new
AI session — paste relevant docs for full context.**

---

## Local Development

### Prerequisites
- Java 25 (Eclipse Temurin) — https://adoptium.net
- Docker Desktop
- IntelliJ IDEA Community Edition
- Node.js 20+

### Run locally
```bash
# Clone repo
git clone https://github.com/ugotalan2/chores

# Start database and API
docker-compose up

# Backend runs at http://localhost:8080
# Frontend runs at http://localhost:4200
```

### Run tests
```bash
# Backend
cd backend && ./mvnw test

# Frontend
cd frontend && npm run test:ci
```

### Format code
```bash
# Backend — auto-fix formatting
cd backend && ./mvnw spotless:apply

# Frontend — auto-fix formatting
cd frontend && npm run lint:fix
```

---

## Branch Strategy
```
main      ← production
develop   ← integration
feature/* ← your work
```
Never commit directly to main or develop.
Pre-commit hooks run tests and formatting checks
before every commit.

---

## Version Roadmap

**V1 — MVP (Summer 2026)**
Core chore verification, photo AI, parent dashboard,
Saturday rotation, bathroom rotation, dinner tracking

**V2 — School Schedules**
School vs non-school days, face login, chore
instructions, grounding tracker

**V3 — Home Hub**
Wall tablet, Wyze cameras, dock alarm, MDM lockdown,
family calendar and shopping list

---

## Hardware
- Chore device: Samsung Galaxy A14 (unlocked, $89)
- Carrier: TextNow free SIM ($0/month)
- See HARDWARE.md for full hardware decisions