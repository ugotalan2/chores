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

## Template for new gotchas:

## GOTCHA-XXX — Short Title
**Problem:** What goes wrong and when
**Fix:** How to prevent or resolve it
**Files affected:** Which files need attention