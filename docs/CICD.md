# CICD.md

## Overview
ChoreHouse uses GitHub Actions for CI/CD, Docker for
containerization, and Railway for hosting. The pipeline
is designed to mimic enterprise workflow patterns
(feature branches, automated testing, database migrations)
while remaining free to operate.

The goal is to build professional workflow habits that
transfer directly to enterprise environments using
Jenkins/Azure/Kubernetes — the concepts are identical,
only the tooling syntax differs.

---

## Java Version
**Use Java 21 LTS**
- Current enterprise standard
- Fully supported by Spring Boot 3.x
- Free Oracle JDK updates until September 2026
- Plan migration to Java 25 LTS before Sept 2026
- Download from: https://adoptium.net (Temurin
  distribution — always free, no license concerns)

**Why Temurin over Oracle JDK:**
Eclipse Temurin is the community OpenJDK build.
Identical performance, no licensing complications,
free forever. Most enterprise teams use OpenJDK
builds rather than Oracle JDK for this reason.

---

## IDE — IntelliJ IDEA
**Use IntelliJ IDEA Community Edition (free)**
Download: https://www.jetbrains.com/idea/

**Why IntelliJ over VSCode for Java:**
- Industry standard for enterprise Java teams
- Superior refactoring tools
- Deeper Spring Boot awareness
- Better code completion and inspections
- Built-in database tools (query PostgreSQL directly)
- Maven/Gradle integration is seamless
- Most professional Java teams use IntelliJ

**Key IntelliJ plugins to install:**
- Spring Boot (bundled in Ultimate, plugin for Community)
- Lombok
- SonarLint
- Docker
- GitToolBox
- Conventional Commits (for commit message formatting)
- Database tools (bundled — connect directly to
  local PostgreSQL)

---

## Branch Strategy
Standard enterprise feature branch workflow:

```
main          ← production, fully protected
  ↑
develop       ← integration branch
  ↑
feature/*     ← your working branches

Naming convention:
  feature/backend-user-model
  feature/chore-verification-api
  feature/parent-dashboard-review-queue
  bugfix/streak-calculation-timezone
  hotfix/submission-photo-upload
```

**Rules:**
- Never commit directly to main or develop
- All work done on feature branches
- PR to develop when feature complete
- develop merges to main for deployment
- Squash merge to keep history clean

---

## Pre-Commit Hooks — Husky
Runs checks locally BEFORE commit reaches GitHub.
Broken code never hits the branch. No email floods.

**Install:**
```bash
npm install husky --save-dev
npx husky init
```

**Configure .husky/pre-commit:**
```bash
#!/bin/sh
echo "🔍 Running pre-commit checks..."

# Backend formatting check
cd backend
./mvnw spotless:check
if [ $? -ne 0 ]; then
  echo "❌ Java formatting failed."
  echo "   Run ./mvnw spotless:apply to auto-fix."
  exit 1
fi

# Backend tests
./mvnw test
if [ $? -ne 0 ]; then
  echo "❌ Backend tests failed. Fix before committing."
  exit 1
fi

# Frontend lint
cd ../frontend
npm run lint
if [ $? -ne 0 ]; then
  echo "❌ Frontend lint failed."
  exit 1
fi

# Frontend tests
npm run test:ci
if [ $? -ne 0 ]; then
  echo "❌ Frontend tests failed."
  exit 1
fi

echo "✅ All checks passed. Committing..."
```

**If checks fail:**
- Commit is blocked entirely — nothing pushed
- Fix the issue locally
- Run spotless:apply or lint:fix for auto-fixable issues
- Commit again

---

## Code Formatting

### Backend — Spotless + Google Java Format
```xml
<!-- pom.xml -->
<plugin>
  <groupId>com.diffplug.spotless</groupId>
  <artifactId>spotless-maven-plugin</artifactId>
  <version>2.43.0</version>
  <configuration>
    <java>
      <googleJavaFormat>
        <version>1.19.1</version>
      </googleJavaFormat>
    </java>
  </configuration>
</plugin>
```

Commands:
- `./mvnw spotless:check` — fails if formatting off
- `./mvnw spotless:apply` — auto-fixes all formatting

### Frontend — ESLint + Prettier
```json
// .prettierrc
{
  "singleQuote": true,
  "semi": true,
  "tabWidth": 2,
  "printWidth": 80
}
```

Commands:
- `npm run lint` — check formatting
- `npm run lint:fix` — auto-fix formatting

---

## GitHub Actions Pipeline

```yaml
# .github/workflows/ci.yml

name: ChoreHouse CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:

  backend-test:
    name: Backend Tests
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: chorehouse_test
          POSTGRES_USER: chorehouse
          POSTGRES_PASSWORD: testpassword
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: ./mvnw verify
        working-directory: backend
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/chorehouse_test
          SPRING_DATASOURCE_USERNAME: chorehouse
          SPRING_DATASOURCE_PASSWORD: testpassword

  frontend-test:
    name: Frontend Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: npm ci
        working-directory: frontend
      - run: npm run lint
        working-directory: frontend
      - run: npm run test:ci
        working-directory: frontend

  build:
    name: Build Docker Images
    needs: [backend-test, frontend-test]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' ||
        github.ref == 'refs/heads/develop'
    steps:
      - uses: actions/checkout@v4
      - name: Login to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - name: Build and push backend
        uses: docker/build-push-action@v5
        with:
          context: ./backend
          push: true
          tags: |
            ghcr.io/${{ github.repository }}/backend:latest
            ghcr.io/${{ github.repository }}/backend:${{ github.sha }}
      - name: Build and push frontend
        uses: docker/build-push-action@v5
        with:
          context: ./frontend
          push: true
          tags: |
            ghcr.io/${{ github.repository }}/frontend:latest
            ghcr.io/${{ github.repository }}/frontend:${{ github.sha }}

  deploy:
    name: Deploy to Railway
    needs: [build]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to Railway
        uses: bervProject/railway-deploy@main
        with:
          railway_token: ${{ secrets.RAILWAY_TOKEN }}
          service: chorehouse-api
```

---

## Docker Setup

### docker-compose.yml (local development)
```yaml
services:
  api:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/chorehouse
      SPRING_DATASOURCE_USERNAME: chorehouse
      SPRING_DATASOURCE_PASSWORD: localpassword
      TZ: America/Chicago
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:16
    environment:
      POSTGRES_DB: chorehouse
      POSTGRES_USER: chorehouse
      POSTGRES_PASSWORD: localpassword
      TZ: America/Chicago
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U chorehouse"]
      interval: 10s
      timeout: 5s
      retries: 5

  frontend:
    build: ./frontend
    ports:
      - "4200:4200"
    volumes:
      - ./frontend/src:/app/src

volumes:
  postgres_data:
```

### backend/Dockerfile
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENV TZ=America/Chicago
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### frontend/Dockerfile
```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json .
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist/chorehouse /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

---

## Database Migrations — Flyway

Every database schema change is a versioned SQL file.
Never alter the database manually. Always through
a migration file committed to Git.

**File location:**
```
backend/src/main/resources/db/migration/
  V1__create_users_table.sql
  V2__create_chores_table.sql
  V3__create_required_shots_table.sql
  V4__create_assignments_table.sql
  V5__create_schedule_config_table.sql
  V6__create_daily_chore_instances_table.sql
  V7__create_shot_submissions_table.sql
  V8__create_rotation_history_table.sql
  V9__create_streaks_table.sql
  V10__create_notifications_table.sql
```

**Naming convention:**
V{number}__{description_with_underscores}.sql

**Flyway runs automatically on app startup.**
CI pipeline runs migrations against test DB before
deploying to production.

**pom.xml dependency:**
```xml
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

---

## Authentication — Clerk

**Parent PWA:**
- Full Clerk OAuth (Google sign-in recommended)
- Clerk issues JWT → Spring Boot validates via JWKS
- ROLE_PARENT assigned on first login
- Second parent invited via Clerk invitation flow

**Chore Device (Android):**
- One-time setup: parent signs in via Clerk on device
- Device receives long-lived device token
- Token stored securely in Android Keystore
- All subsequent API calls use device token
- Kids never see any auth screen after setup
- Device token scoped to ROLE_DEVICE

**Three auth roles:**

| Role | Auth Method | Access |
|---|---|---|
| ROLE_PARENT | Clerk OAuth JWT | Full PWA access |
| ROLE_DEVICE | Long-lived device token | Kid chore endpoints only |
| ROLE_CHILD | None — covered by device token | N/A |

**Spring Boot Clerk validation:**
```java
@Bean
public SecurityFilterChain filterChain(
    HttpSecurity http) throws Exception {
  http
    .oauth2ResourceServer(oauth2 -> oauth2
      .jwt(jwt -> jwt
        .jwkSetUri(clerkJwksUri)))
    .authorizeHttpRequests(auth -> auth
      .requestMatchers("/api/parent/**")
        .hasRole("PARENT")
      .requestMatchers("/api/device/**")
        .hasRole("DEVICE")
      .anyRequest().authenticated());
  return http.build();
}
```

---

## Hosting — Railway

- Spring Boot API hosted on Railway free tier
- PostgreSQL database hosted on Railway free tier
- Both in same Railway project — one dashboard
- Angular PWA hosted on Firebase Hosting (free)
- Deploy triggered automatically on merge to main

**Environment variables stored in Railway dashboard:**
- DATABASE_URL
- CLERK_JWKS_URI
- OPENAI_API_KEY
- FIREBASE_FCM_KEY
- TZ=America/Chicago

---

## Repository Structure

```
chorehouse/
  ├── .github/
  │   └── workflows/
  │       └── ci.yml
  ├── .husky/
  │   └── pre-commit
  ├── backend/
  │   ├── src/
  │   │   ├── main/
  │   │   │   ├── java/com/chorehouse/
  │   │   │   └── resources/
  │   │   │       ├── db/migration/
  │   │   │       └── application.properties
  │   ├── Dockerfile
  │   └── pom.xml
  ├── frontend/
  │   ├── src/
  │   ├── Dockerfile
  │   └── package.json
  ├── docs/
  │   ├── PROJECT_OVERVIEW.md
  │   ├── ARCHITECTURE.md
  │   ├── FEATURES.md
  │   ├── HARDWARE.md
  │   ├── DECISIONS.md
  │   ├── DATA_MODEL.md
  │   ├── CICD.md
  │   └── GOTCHAS.md
  ├── docker-compose.yml
  └── README.md
```