# HireTrack — Backend

> REST API for a Canadian job application intelligence platform. Built with Spring Boot 4, Spring Security, and JJWT — featuring stateless JWT authentication, a web scraping engine (Jsoup), automated email reminders, and a skill-gap analysis algorithm.

---

## What this is

HireTrack is a full-stack job tracking application designed for the Canadian job market. This repository is the backend: a Spring Boot REST API that handles authentication, job data extraction from any posting URL, skill-match scoring, application pipeline management, and scheduled follow-up email reminders.

The frontend (Next.js 15 + React 19) lives in a separate repository and communicates with this API exclusively over HTTP using JWT tokens.

**Current build status:** Authentication layer complete and functional — `User` entity, `UserRepository`, `JwtConfig`, `JwtTokenProvider`, `JwtAuthFilter`, and `UserDetailsServiceImpl` are all implemented. Remaining layers (scraper, skill matcher, application CRUD, dashboard, email scheduler) are actively in development following a 4-week build plan.

---

## Features (completed ✅ / in progress 🔨)

**Authentication & Security** ✅
- User registration with BCrypt password hashing
- JWT login — 24-hour tokens signed with HMAC-SHA256
- `JwtAuthFilter` intercepts every request, validates the token, and loads the authenticated user into Spring's `SecurityContextHolder`
- Stateless session — no cookies, no server-side sessions
- Public routes (`/api/auth/**`) whitelisted; all other routes require a valid Bearer token

**Job Scraper** 🔨
- Paste any job posting URL → Jsoup fetches and parses the raw HTML
- Extracts: title, company, location, description, salary range (regex), required skills (keyword matching against a curated tech skill dictionary)
- Deduplication: checks `jobs` table by URL before hitting the network — never scrapes the same page twice

**Skill Gap Analysis** 🔨
- Compares the user's declared skill profile against a job's extracted required skills
- Set intersection algorithm: `userSkills.retainAll(requiredSkills)` for matched, `requiredSkills.removeAll(userSkills)` for missing
- Outputs a 0–100 match score: `(matched.size() / required.size()) * 100`, credit-weighted
- Saved to `skill_matches` table and returned with every application response

**Application Pipeline** 🔨
- 8-stage pipeline: `SAVED → APPLIED → PHONE_SCREEN → TECHNICAL → FINAL_ROUND → OFFER → REJECTED / WITHDRAWN`
- Full CRUD with ownership verification — users can only modify their own applications
- `resumeVersion` field tracks which resume variant was sent for each application

**Dashboard & Analytics** 🔨
- Aggregate counts per pipeline stage
- Upcoming follow-ups: applications with `followUpDate` within the next 7 days

**Scheduled Email Reminders** 🔨
- `@Scheduled` cron job fires daily at 9:00 AM
- Queries all applications with `followUpDate` before today + 1
- Sends personalised follow-up reminder emails via Gmail SMTP (JavaMail)

**API Documentation** 🔨
- SpringDoc OpenAPI auto-generates Swagger UI at `/swagger-ui.html` from controller annotations — zero manual documentation

---

## Architecture

HireTrack is a decoupled full-stack application. The frontend and backend are separate GitHub repositories, deployed independently, and communicate only through this REST API.

```
Next.js Frontend (Vercel)
        │
        │  HTTPS + JWT Bearer token
        ▼
Spring Boot API (Render.com · port 8080)
        │
        ├── JwtAuthFilter          Validates token on every request
        ├── Controller layer       Thin — receives request, calls service, returns ResponseEntity
        ├── Service layer          All business logic lives here
        ├── Repository layer       JpaRepository — Spring generates SQL from method names
        │
        ▼
MySQL 8 Database (port 3306)
```

**Request lifecycle:**
```
HTTP request → JwtAuthFilter → Controller → Service → Repository → MySQL
                                                                      ↓
HTTP response ← Controller ← ResponseEntity ← DTO mapping ← Entity ←┘
```

---

## Project structure

```
src/main/java/com/hiretrack/
│
├── HiretrackApplication.java          @SpringBootApplication entry point
│
├── config/
│   ├── JwtConfig.java                 Reads jwt.secret + jwt.expiration from application.properties
│   ├── SecurityConfig.java            Filter chain — permits /api/auth/**, requires auth elsewhere, STATELESS session
│   └── CorsConfig.java                Allows localhost:3000 and Vercel frontend to call the API
│
├── model/                             JPA @Entity classes — Hibernate auto-creates tables
│   ├── User.java                      ✅ implements UserDetails — email as principal, BCrypt password
│   ├── Job.java                       Scraped job data: url, title, company, salary, requiredSkills
│   ├── JobApplication.java            Links User ↔ Job with status, dates, notes, resumeVersion
│   ├── ApplicationStatus.java         Enum: SAVED|APPLIED|PHONE_SCREEN|TECHNICAL|FINAL_ROUND|OFFER|REJECTED|WITHDRAWN
│   ├── SkillMatch.java                Algorithm result: matchScore, matchedSkills, missingSkills
│   └── UserSkill.java                 User's declared skills with Proficiency enum (BEGINNER|INTERMEDIATE|ADVANCED)
│
├── dto/                               Data Transfer Objects — never expose raw @Entity over HTTP
│   ├── request/                       LoginRequest, RegisterRequest, ApplicationRequest, ScrapeRequest, SkillRequest
│   └── response/                      AuthResponse, ApplicationResponse, JobResponse, SkillMatchResponse, DashboardResponse
│
├── repository/                        JpaRepository interfaces — Spring generates all SQL
│   ├── UserRepository.java            ✅ findByEmail(), existsByEmail()
│   ├── ApplicationRepository.java     findByUser(), findByUserAndStatus(), findByFollowUpDateBefore(), countByUserAndStatus()
│   ├── JobRepository.java             findByUrl() — deduplication check before scraping
│   ├── SkillRepository.java           findByUser(), findByUserAndSkillNameIgnoreCase()
│   └── SkillMatchRepository.java      findByApplication()
│
├── service/
│   ├── AuthService.java               register() → BCrypt + save + JWT · login() → authenticate + JWT
│   ├── ApplicationService.java        CRUD with ownership verification via user ID check
│   ├── JobScraperService.java         Jsoup scraper + extractSkills() keyword matcher + extractSalary() regex
│   ├── SkillMatcherService.java       Set intersection algorithm → 0–100 match score
│   ├── EmailReminderService.java      JavaMailSender → personalised follow-up emails
│   └── DashboardService.java          Aggregates counts, upcoming follow-ups → DashboardResponse
│
├── controller/                        @RestController — thin, delegates to services
│   ├── AuthController.java            POST /api/auth/register · POST /api/auth/login
│   ├── ApplicationController.java     GET|POST|PUT|DELETE /api/applications · /api/applications/{id}
│   ├── JobController.java             POST /api/jobs/scrape · GET /api/jobs/{id}
│   ├── SkillController.java           GET|POST|DELETE /api/skills · POST /api/skills/match/{appId}
│   └── DashboardController.java       GET /api/dashboard/stats · /funnel · /upcoming
│
├── security/
│   ├── JwtTokenProvider.java          ✅ generateToken() · validateToken() · getEmailFromToken() — JJWT 0.13
│   ├── JwtAuthFilter.java             ✅ OncePerRequestFilter — extracts Bearer token, validates, sets SecurityContext
│   └── UserDetailsServiceImpl.java    ✅ loadUserByUsername(email) → UserRepository lookup
│
├── exception/
│   ├── GlobalExceptionHandler.java    @RestControllerAdvice — returns JSON errors for 400/403/404/500
│   ├── ResourceNotFoundException.java Custom 404
│   └── UnauthorizedException.java     Custom 403
│
└── scheduler/
    └── ReminderScheduler.java         @Scheduled(cron) — fires daily at 9:00 AM, calls EmailReminderService
```

---

## Tech stack

| Layer | Technology | Version |
|---|---|---|
| Framework | Spring Boot | 4.0.6 |
| Language | Java | 25 |
| Security | Spring Security | 4.1.0 |
| Authentication | JJWT (HMAC-SHA256) | 0.13.0 |
| Database | MySQL 8 + Spring Data JPA / Hibernate | 9.7.0 (connector) |
| Web scraping | Jsoup | 1.22.2 |
| Email | Spring Mail (JavaMail / Gmail SMTP) | 4.1.0 |
| API docs | SpringDoc OpenAPI (Swagger UI) | 3.0.3 |
| Boilerplate reduction | Lombok | 1.18.46 |
| Entity ↔ DTO mapping | ModelMapper | 3.2.6 |
| String utilities | Apache Commons Lang | 3.20.0 |
| Testing | JUnit 5 + Mockito + Spring Test | — |
| Test DB | H2 in-memory | 2.4.240 |
| Build | Maven | — |
| Deployment | Render.com | — |

---

## Database schema

Five tables — auto-created by Hibernate on startup via `ddl-auto=update`. No SQL scripts to run.

```
users
├── id            BIGINT PK AUTO_INCREMENT
├── name          VARCHAR(100) NOT NULL
├── email         VARCHAR(100) NOT NULL UNIQUE
├── password      VARCHAR(255) NOT NULL  ← BCrypt hash
└── created_at    TIMESTAMP

jobs
├── id            BIGINT PK AUTO_INCREMENT
├── url           TEXT NOT NULL          ← deduplication key
├── title         VARCHAR(200)
├── company       VARCHAR(200)
├── location      VARCHAR(200)
├── salary_min    INT NULLABLE
├── salary_max    INT NULLABLE
├── description   TEXT
├── required_skills TEXT                 ← comma-separated, extracted by Jsoup
└── scraped_at    TIMESTAMP

job_applications
├── id            BIGINT PK AUTO_INCREMENT
├── user_id       BIGINT FK → users.id
├── job_id        BIGINT FK → jobs.id
├── status        ENUM (8 stages)
├── applied_date  DATE
├── follow_up_date DATE                  ← triggers email reminder
├── notes         TEXT
├── resume_version VARCHAR(100)
├── created_at    TIMESTAMP
└── updated_at    TIMESTAMP ON UPDATE

user_skills
├── id            BIGINT PK AUTO_INCREMENT
├── user_id       BIGINT FK → users.id
├── skill_name    VARCHAR(100)
└── proficiency   ENUM (BEGINNER|INTERMEDIATE|ADVANCED)

skill_matches
├── id            BIGINT PK AUTO_INCREMENT
├── application_id BIGINT FK → job_applications.id UNIQUE
├── match_score   INT (0–100)
├── matched_skills TEXT                  ← comma-separated
├── missing_skills TEXT                  ← comma-separated
└── calculated_at  TIMESTAMP
```

---

## REST API endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user, returns JWT |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| GET | `/api/applications` | JWT | Get all applications for current user |
| POST | `/api/applications` | JWT | Create new application |
| GET | `/api/applications/{id}` | JWT | Get single application |
| PUT | `/api/applications/{id}` | JWT | Update status / notes |
| DELETE | `/api/applications/{id}` | JWT | Delete application (ownership verified) |
| POST | `/api/jobs/scrape` | JWT | Scrape job URL, returns extracted Job |
| GET | `/api/jobs/{id}` | JWT | Get job by ID |
| GET | `/api/skills` | JWT | Get user's skill profile |
| POST | `/api/skills` | JWT | Add a skill |
| DELETE | `/api/skills/{id}` | JWT | Remove a skill |
| POST | `/api/skills/match/{appId}` | JWT | Run skill-match algorithm for an application |
| GET | `/api/dashboard/stats` | JWT | Aggregate counts + upcoming follow-ups |
| GET | `/api/dashboard/funnel` | JWT | Per-stage counts for pipeline chart |
| GET | `/api/dashboard/upcoming` | JWT | Applications with follow-up due within 7 days |

Full interactive documentation available at `/swagger-ui.html` once the server is running.

---

## Getting started

**Prerequisites:** Java 21+, Maven 3+, MySQL 8+

```bash
# 1. Clone
git clone https://github.com/Bhavya-mahyavanshi/HireTrack_Backend.git
cd HireTrack_Backend

# 2. Create the database
mysql -u root -p -e "CREATE DATABASE hiretrack_db;"

# 3. Configure environment
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Edit application.properties — fill in DB password, JWT secret, Gmail credentials

# 4. Run
./mvnw spring-boot:run
# → API running at http://localhost:8080
# → Swagger UI at http://localhost:8080/swagger-ui.html
```

**application.properties values you must set:**

```properties
spring.datasource.password=your_mysql_password

# Generate with: openssl rand -base64 32
jwt.secret=your_base64_encoded_secret_min_32_chars

spring.mail.username=your_gmail@gmail.com
spring.mail.password=your_16_char_app_password   # Gmail App Password, not your login password
```

---

## Running tests

```bash
./mvnw test
```

Tests use H2 in-memory database — no MySQL connection required. `@WithMockUser` from Spring Security Test is used to simulate authenticated requests on secured endpoints.

---

## JWT authentication flow

```
POST /api/auth/register  { name, email, password }
        ↓
AuthService hashes password with BCrypt
        ↓
User saved to MySQL
        ↓
JwtTokenProvider.generateToken(email) → signed HMAC-SHA256 JWT (24h expiry)
        ↓
{ token: "eyJ...", type: "Bearer", email, name }

─────────────────────────────────────────────────

Subsequent request: Authorization: Bearer eyJ...
        ↓
JwtAuthFilter.extractToken()  →  authHeader.substring(7)
        ↓
JwtTokenProvider.validateToken()  →  parse + verify signature
        ↓
JwtTokenProvider.getEmailFromToken()  →  claims.getSubject()
        ↓
UserDetailsServiceImpl.loadUserByUsername(email)  →  DB lookup
        ↓
SecurityContextHolder.getContext().setAuthentication(...)
        ↓
Controller receives authenticated request
```

---

## Skill match algorithm

The core feature that differentiates HireTrack from a spreadsheet tracker.

```java
// 1. Load user's declared skills → Set<String> (lowercase)
Set<String> userSkills = skillRepository.findByUser(user)
    .stream()
    .map(s -> s.getSkillName().toLowerCase())
    .collect(Collectors.toSet());

// 2. Load job's required skills → Set<String> (split from comma-separated string)
Set<String> requiredSkills = Arrays.stream(
    job.getRequiredSkills().split(","))
    .map(String::trim)
    .map(String::toLowerCase)
    .collect(Collectors.toSet());

// 3. Intersection = matched
Set<String> matched = new HashSet<>(userSkills);
matched.retainAll(requiredSkills);

// 4. Difference = missing
Set<String> missing = new HashSet<>(requiredSkills);
missing.removeAll(userSkills);

// 5. Score
int score = (int) ((matched.size() / (double) requiredSkills.size()) * 100);
```

Result is persisted in `skill_matches` and returned with every `ApplicationResponse` so the frontend can display it without an extra round-trip.

---

## Deployment

**Backend → Render.com**

1. Connect the GitHub repository in Render dashboard
2. Build command: `./mvnw clean package -DskipTests`
3. Start command: `java -jar target/demo-0.0.1-SNAPSHOT.jar`
4. Set environment variables in Render dashboard (never commit secrets):
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_PASSWORD`
   - `JWT_SECRET`
   - `SPRING_MAIL_PASSWORD`

**Frontend → Vercel** (separate repo)

Set `NEXT_PUBLIC_API_URL=https://your-app.onrender.com` in Vercel project settings.

---

## Related repository

| Repo | Stack | Description |
|---|---|---|
| `HireTrack_Backend` (this repo) | Spring Boot · MySQL · JWT | REST API |
| `hiretrack-frontend` *(coming)* | Next.js 15 · Jotai · SWR · Tailwind | Web UI |

---

## Author

**Bhavya Mahyavanshi** · Java Full-Stack Developer

[LinkedIn](https://linkedin.com/in/bhavya-mahyavanshi) · [GitHub](https://github.com/Bhavya-mahyavanshi) · [Portfolio](https://bhavya-mahyavanshi.vercel.app)
