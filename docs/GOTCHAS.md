# GOTCHAS.md

## Purpose
Known pitfalls, bugs, and technical issues discovered
during planning and development. Check this file first
when something behaves unexpectedly. Update whenever
a new gotcha is found.

---

## GOTCHA-001 — Timezone / Date Shifting
**Problem:** PostgreSQL stores timestamps in UTC.
Without explicit timezone configuration, dates shift
when displayed in local time. A chore submitted at
11pm CST appears as the next day in UTC. This breaks
daily chore instance logic, streak calculation, and
the next-day device flag.

**This app's timezone:** America/Chicago (CST/CDT)
Kansas City, Missouri
- UTC-6 in winter (CST)
- UTC-5 in summer (CDT)

**Fix — apply ALL of the following:**

1. Set JVM default timezone at Spring Boot startup:
```java
@SpringBootApplication
public class ChoresApplication {
  public static void main(String[] args) {
    TimeZone.setDefault(
      TimeZone.getTimeZone("America/Chicago"));
    SpringApplication.run(
      ChoresApplication.class, args);
  }
}
```

2. Set Hibernate timezone in application.properties:
```properties
spring.jpa.properties.hibernate.jdbc.time_zone=
  America/Chicago
```

3. Use TIMESTAMP WITH TIME ZONE in PostgreSQL for
   all timestamp columns (not plain TIMESTAMP)

4. Use DATE type (not TIMESTAMP) for date-only fields
   like dailyChoreInstances.date — DATE columns have
   no timezone component and never shift

5. In Angular templates always pass timezone:
```typescript
{{ value | date:'MM/dd/yyyy':'America/Chicago' }}
```

6. In API responses always include timezone offset:
   "2026-06-01T23:00:00-05:00" not
   "2026-06-01T23:00:00Z"

7. Set TZ environment variable in Docker and Railway:
```yaml
# docker-compose.yml
environment:
  TZ: America/Chicago
```
```
# Railway environment variable
TZ=America/Chicago
```

**Files that need timezone attention:**
- ChoresApplication.java
- application.properties
- All JPA entity classes with date/timestamp fields
- All Flyway migration files (use TIMESTAMP WITH
  TIME ZONE and DATE column types)
- All Angular components displaying dates
- Daily instance generation scheduled job
- Streak calculation service
- Next-day device flag logic

---

## GOTCHA-002 — Scheduled Job Timezone
**Problem:** Spring Boot @Scheduled cron jobs run in
JVM timezone. If JVM timezone not set correctly the
midnight daily instance generation job fires at wrong
time. Midnight UTC = 6am or 7am CST — chores for
the day would generate hours late.

**Fix:** Always specify zone explicitly on @Scheduled.
Do not rely on JVM default even if GOTCHA-001 is
applied:
```java
@Scheduled(
  cron = "0 0 0 * * *",
  zone = "America/Chicago"
)
public void generateDailyInstances() {
  // runs at midnight CST/CDT
}
```

---

## GOTCHA-003 — Firebase FCM Token Refresh
**Problem:** Firebase Cloud Messaging tokens expire
and refresh periodically. If the app doesn't handle
token refresh, push notifications silently fail with
no error visible to the user or parent.

**Fix:** Implement FirebaseMessagingService in the
Android Capacitor app to listen for token refresh
events and POST the new token to the Spring Boot
backend immediately:
```java
// Android service
@Override
public void onNewToken(String token) {
  // POST to /api/device/fcm-token
}
```
Store updated token in users table fcmToken field.

---

## GOTCHA-004 — Java Version Licensing

**Fix:** Use Eclipse Temurin (OpenJDK) distribution
instead of Oracle JDK — always free, no licensing
issues. Download from https://adoptium.net

In Dockerfile always use Temurin base image:
```dockerfile
FROM eclipse-temurin:25-jdk-alpine AS builder
FROM eclipse-temurin:25-jre-alpine
```

---

## GOTCHA-005 — Flyway Migration Immutability
**Problem:** Once a Flyway migration file is committed
and run against any database (local, CI, or production)
it must NEVER be modified. Flyway checksums each file
and will throw an error on startup if a previously
run migration file has changed.

**Fix:** Migration files are write-once. If you need
to change something already migrated:
- Create a NEW migration file with the next version
  number that alters the existing schema
- Never edit a migration file that has already run
- If caught early in local dev only: delete local
  database volume and re-run from scratch

```bash
# Reset local database only — never do this in prod
docker-compose down -v
docker-compose up
```

---

## GOTCHA-006 — Clerk JWT Validation in Spring Boot
**Problem:** Clerk rotates its JWT signing keys
periodically. If Spring Security caches the JWKS
keys indefinitely, token validation will fail after
a key rotation with a cryptic 401 error.

**Fix:** Configure Spring Security to refresh JWKS
keys periodically:
```java
@Bean
JwtDecoder jwtDecoder() {
  NimbusJwtDecoder decoder =
    NimbusJwtDecoder
      .withJwkSetUri(clerkJwksUri)
      .build();
  // Refresh keys every hour
  decoder.setJwkSetCache(
    new DefaultJWKSetCache(
      1, 1, TimeUnit.HOURS));
  return decoder;
}
```

---

## GOTCHA-007 — AI Photo Verification Cost Control
**Problem:** GPT-4o Vision API is pay-per-use. With
5 kids, multiple chores, multiple shots per chore,
and potential re-submissions, costs could accumulate
unexpectedly if not monitored.

**Fix:**
- Set a monthly spend limit in OpenAI dashboard
- Log every API call with token count in the
  shot_submissions table
- Add a Spring Boot health endpoint that reports
  current month AI spend
- Consider caching AI verdicts — if same photo URL
  is submitted twice return cached result rather
  than calling API again
- Alert parent if daily AI spend exceeds threshold

---

## GOTCHA-008 — Outside Photo AI Unreliability
**Problem:** AI photo verification is unreliable for
outdoor photos. Variable lighting, changing backgrounds
(weather, shadows, time of day), and lack of a
consistent reference baseline make outdoor reference
comparison and clutter detection both fail frequently.

**Fix:** Never use AI_RELIABLE or AI_WITH_OVERRIDES
for outdoor chore shots. Two options:
1. Set outdoor shots to PARENT_QUEUE_ONLY
2. Reframe the shot as an indoor storage location
   (equipment bin, garage wall mount) where a clean
   reference photo exists and lighting is consistent.
   This is the preferred approach for sporting goods —
   verify the equipment is put away in its storage
   location rather than trying to verify the yard
   is clear.

**Affected chore:** Kid 4 sporting goods outdoor shot.
Configure as storage location photo with
PARENT_QUEUE_ONLY or AI_WITH_OVERRIDES depending
on how consistent the storage location lighting is.

---

## GOTCHA-009 — AI Prompt Specificity for Category Chores
**Problem:** House rotation chores are category-specific
(trash ONLY, clothes ONLY, toys ONLY). Without a
precise AI prompt, GPT-4o may fail a room because
it sees clothes on the floor when the kid's chore
is only trash pickup — creating false failures and
frustrated kids.

**Fix:** AI prompts for category chores must explicitly
state what to ignore. Example for Kid 1 trash chore:

"This chore is PICK UP TRASH ONLY. The kid is
responsible for removing trash (wrappers, cups,
paper scraps, food containers) from this room.
Ignore any clothes, shoes, toys, or sporting goods
— those are other kids' responsibilities. Return
PASS if no visible trash is present on floors or
surfaces. Return FAIL with specific location of
trash found if trash is visible."

Each category chore needs its own precisely scoped
AI prompt. Parent configures this per chore in the
dashboard during setup. Getting these prompts right
is critical to reducing false failures and parent
override fatigue.

## GOTCHA-010 — Spotless Incompatibility with Java 25
**Problem:** Spotless maven plugin versions below 2.46.0
are incompatible with Java 25. Throws NoSuchMethodError
on DeferredDiagnosticHandler.getDiagnostics() because
Java 25 changed the return type from Queue to List.

**Fix:** Use spotless-maven-plugin 2.46.0 or higher
with googleJavaFormat 1.28.0 or higher in pom.xml.

**Files affected:** pom.xml

**Files affected:**
- Parent dashboard chore setup UI
- required_shots.ai_prompt field
- Spring Boot AI verification service

## GOTCHA-012 — Spring Boot 4 Flyway Starter Required
**Problem:** Spring Boot 4 split autoconfiguration into
separate modules. Unlike Spring Boot 3, simply adding
flyway-core to pom.xml is not enough — Flyway will
silently not run. No errors, no logs, nothing.

**Fix:** Use spring-boot-starter-flyway instead of
flyway-core. Keep flyway-database-postgresql as well.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

**Files affected:** pom.xml

## Template for new gotchas:

## GOTCHA-XXX — Short Title
**Problem:** What goes wrong and when
**Fix:** How to prevent or resolve it
**Files affected:** Which files need attention