<div align="center">

# 🎯 HireTrack

### Canadian Job Application Intelligence Platform

[![Backend CI](https://img.shields.io/github/actions/workflow/status/Bhavya-mahyavanshi/hiretrack-backend/ci.yml?label=Backend%20CI&style=flat-square&logo=github)](https://github.com/Bhavya-mahyavanshi/hiretrack-backend)
[![Frontend CI](https://img.shields.io/github/actions/workflow/status/Bhavya-mahyavanshi/hiretrack-frontend/ci.yml?label=Frontend%20CI&style=flat-square&logo=github)](https://github.com/Bhavya-mahyavanshi/hiretrack-frontend)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-15-000000?style=flat-square&logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

<br/>

> **Stop tracking job applications in spreadsheets.**
> Paste any job URL → HireTrack auto-extracts the role, company, salary, and required skills,
> scores your match percentage, and reminds you when to follow up.

<br/>

**[🚀 Live Demo](https://hiretrack.vercel.app)** &nbsp;·&nbsp;
**[📖 API Docs (Swagger)](https://hiretrack-api.render.com/swagger-ui.html)** &nbsp;·&nbsp;
**[🐛 Report a Bug](https://github.com/Bhavya-mahyavanshi/hiretrack-backend/issues)** &nbsp;·&nbsp;
**[💡 Request a Feature](https://github.com/Bhavya-mahyavanshi/hiretrack-backend/issues)**

<br/>

![HireTrack Dashboard Screenshot](https://placehold.co/900x480/0A0F2C/00D4FF?text=Dashboard+Screenshot+Here)

</div>

---

## 📋 Table of Contents

- [Why HireTrack](#-why-hiretrack)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Project Structure](#-project-structure)
- [Running Tests](#-running-tests)
- [Deployment](#-deployment)
- [Roadmap](#-roadmap)
- [Author](#-author)

---

## 💡 Why HireTrack

I built HireTrack because I was living the problem.

Applying to 50+ jobs across Toronto while studying full-time meant juggling spreadsheets, browser tabs, sticky notes, and missed follow-ups. Every existing solution was either bloated, US-focused, or missing the features that actually matter.

So I built the tool I wished existed — and built it with the exact stack Canadian employers are hiring for.

**The result:** a full-stack production application demonstrating end-to-end Java/Spring Boot backend development, REST API design, JWT authentication, web scraping, automated scheduling, and a modern React frontend — deployed and live.

---

## ✨ Features

### 🔍 Smart Job Scraping
Paste any job posting URL from LinkedIn, Indeed, or a company careers page. HireTrack automatically extracts the job title, company, location, salary range, and required skills — no manual data entry.

### 📊 Skill Gap Analysis
Every application gets a **match score (0–100%)** calculated by comparing the job's required skills against your personal skill profile. See exactly which skills you have and which ones are missing — so you know where to focus your learning.

### 🗂️ Pipeline Tracking
Track every application through **8 pipeline stages**:
`Saved → Applied → Phone Screen → Technical → Final Round → Offer → Rejected → Withdrawn`

### 📈 Analytics Dashboard
A real-time dashboard showing your application funnel, stage breakdown, weekly activity, and upcoming follow-up deadlines — all in one view.

### ⏰ Automated Email Reminders
Set a follow-up date on any application. HireTrack's Spring `@Scheduled` job runs every morning at 9AM and sends you a reminder email for any applications due that day — so no opportunity falls through the cracks.

### 🔐 Secure Authentication
JWT-based authentication with BCrypt password hashing. Stateless, scalable, and following industry security standards.

### 📄 Swagger API Documentation
Full auto-generated API documentation available at `/swagger-ui.html` — every endpoint documented, testable in the browser.

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot | 3.2 | REST API framework |
| Spring Security | 6 | Authentication & authorization |
| JSON Web Token (JWT) | 0.12.3 | Stateless token-based auth |
| Spring Data JPA | 3.2 | ORM & repository pattern |
| Hibernate | 6 | Database persistence layer |
| MySQL | 8.0 | Relational database |
| Jsoup | 1.17.2 | HTML parsing & job scraping |
| JavaMail (Spring Mail) | 3.2 | Automated email reminders |
| SpringDoc OpenAPI | 2.3 | Swagger UI auto-generation |
| JUnit 5 + Mockito | — | Unit & integration testing |
| Maven | 3.9 | Build & dependency management |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| Next.js | 15 | React framework with file routing |
| React | 19 | Component-based UI |
| Jotai | 2 | Global state management |
| SWR | 2 | Data fetching with caching |
| Axios | 1.7 | HTTP client with interceptors |
| Chart.js | 4 | Pipeline funnel visualization |
| Tailwind CSS | 3 | Utility-first styling |

### DevOps & Infrastructure
| Technology | Purpose |
|---|---|
| GitHub Actions | CI/CD — auto-test on every push |
| Vercel | Frontend deployment |
| Render.com | Backend API deployment |
| Docker | Containerization (coming in v2) |

---

## 🏗 Architecture

HireTrack is a **fully decoupled full-stack application**. The frontend and backend are separate repositories, communicate only via REST API, and deploy independently.

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                            │
│              Next.js 15 + React 19 (Vercel)                 │
│         Jotai State │ SWR Fetching │ Axios HTTP             │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS / REST (JSON)
                           │ Authorization: Bearer <JWT>
┌──────────────────────────▼──────────────────────────────────┐
│                     API LAYER                               │
│           Spring Boot 3 REST API (Render.com)               │
│                                                             │
│  JwtAuthFilter → Controller → Service → Repository          │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Auth Service│  │ Scraper      │  │ Skill Matcher    │  │
│  │  JWT + BCrypt│  │ Service      │  │ Service          │  │
│  │              │  │ Jsoup        │  │ Gap Algorithm    │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  @Scheduled Reminder Scheduler (9AM daily)           │   │
│  │  Spring Mail → Gmail SMTP → User Inbox               │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │ JPA / Hibernate
┌──────────────────────────▼──────────────────────────────────┐
│                   DATA LAYER                                │
│                  MySQL 8.0 Database                         │
│   users │ jobs │ job_applications │ user_skills │ skill_matches │
└─────────────────────────────────────────────────────────────┘
```

### Request Lifecycle
```
User action in browser
  → Axios adds JWT to Authorization header automatically
    → Spring JwtAuthFilter validates token on every request
      → Controller receives clean, authenticated request
        → Service executes business logic
          → JPA Repository queries MySQL
            → JSON response travels back to React component
              → SWR caches the response for performance
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17+ | [adoptium.net](https://adoptium.net/) |
| Maven | 3.9+ | [maven.apache.org](https://maven.apache.org/) |
| Node.js | 18+ | [nodejs.org](https://nodejs.org/) |
| MySQL | 8.0+ | [mysql.com](https://www.mysql.com/) |
| Git | Latest | [git-scm.com](https://git-scm.com/) |

---

### Backend Setup

**1. Clone the repository**
```bash
git clone https://github.com/Bhavya-mahyavanshi/hiretrack-backend.git
cd hiretrack-backend
```

**2. Create the MySQL database**
```sql
CREATE DATABASE hiretrack_db;
```

**3. Configure application properties**

Edit `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/hiretrack_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

# JWT — use a strong random string, minimum 32 characters
jwt.secret=your_super_secret_key_minimum_256_bits_long_here
jwt.expiration=86400000

# Gmail SMTP (create an App Password at myaccount.google.com/apppasswords)
spring.mail.username=your_gmail@gmail.com
spring.mail.password=your_16_char_app_password
```

**4. Run the application**
```bash
mvn spring-boot:run
```

Backend runs at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

### Frontend Setup

**1. Clone the repository**
```bash
git clone https://github.com/Bhavya-mahyavanshi/hiretrack-frontend.git
cd hiretrack-frontend
```

**2. Install dependencies**
```bash
npm install
```

**3. Configure environment**
```bash
# Create .env.local in the project root
echo "NEXT_PUBLIC_API_URL=http://localhost:8080" > .env.local
```

**4. Run the development server**
```bash
npm run dev
```

Frontend runs at: `http://localhost:3000`

---

## 📖 API Documentation

Full interactive API documentation is auto-generated by SpringDoc OpenAPI.

**Local:** `http://localhost:8080/swagger-ui.html`
**Production:** `https://hiretrack-api.render.com/swagger-ui.html`

### Core Endpoints

```
AUTH
────────────────────────────────────────
POST   /api/auth/register           Register new user
POST   /api/auth/login              Login, receive JWT token

APPLICATIONS                        All routes require: Authorization: Bearer <token>
────────────────────────────────────────
GET    /api/applications            Get all user's applications
POST   /api/applications            Create new application
GET    /api/applications/{id}       Get single application detail
PUT    /api/applications/{id}       Update status, notes, follow-up date
DELETE /api/applications/{id}       Delete application

JOBS
────────────────────────────────────────
POST   /api/jobs/scrape             Scrape job data from URL
GET    /api/jobs/{id}               Get scraped job details

SKILLS
────────────────────────────────────────
GET    /api/skills                  Get user's skill profile
POST   /api/skills                  Add a skill
DELETE /api/skills/{id}             Remove a skill
POST   /api/skills/match/{appId}    Calculate skill match score

DASHBOARD
────────────────────────────────────────
GET    /api/dashboard/stats         Application counts by status
GET    /api/dashboard/funnel        Pipeline data for chart
GET    /api/dashboard/upcoming      Follow-ups due this week
```

---

## 🗄 Database Schema

```
┌──────────────┐         ┌─────────────────────┐
│    users     │         │   job_applications   │
├──────────────┤    1    ├─────────────────────┤    1
│ id (PK)      │────────<│ id (PK)             │────────┐
│ name         │         │ user_id (FK)        │        │
│ email        │         │ job_id (FK)         │        │
│ password     │         │ status (ENUM)       │        │
│ created_at   │         │ applied_date        │        │
└──────────────┘         │ follow_up_date      │        │
                         │ notes               │        │
┌──────────────┐         │ resume_version      │        │
│     jobs     │         │ created_at          │        │
├──────────────┤    1    │ updated_at          │        │
│ id (PK)      │────────<└─────────────────────┘        │
│ url          │                                        │ 1
│ title        │         ┌──────────────────┐           │
│ company      │         │  skill_matches   │           │
│ location     │         ├──────────────────┤           │
│ salary_min   │         │ id (PK)          │           │
│ salary_max   │         │ application_id   │>──────────┘
│ description  │         │ match_score      │
│ req_skills   │         │ matched_skills   │
│ scraped_at   │         │ missing_skills   │
└──────────────┘         │ calculated_at    │
                         └──────────────────┘
┌──────────────┐
│  user_skills │
├──────────────┤
│ id (PK)      │
│ user_id (FK) │
│ skill_name   │
│ proficiency  │
└──────────────┘
```

---

## 📁 Project Structure

### Backend
```
hiretrack-backend/
├── .github/workflows/ci.yml           # GitHub Actions CI pipeline
├── src/main/java/com/hiretrack/
│   ├── HiretrackApplication.java      # Entry point
│   ├── config/
│   │   ├── SecurityConfig.java        # Spring Security + filter chain
│   │   ├── JwtConfig.java             # JWT settings from properties
│   │   └── CorsConfig.java            # Allow frontend origin
│   ├── controller/                    # REST endpoints (thin layer)
│   ├── service/                       # Business logic
│   │   ├── JobScraperService.java     # Jsoup web scraping
│   │   ├── SkillMatcherService.java   # Match scoring algorithm
│   │   └── EmailReminderService.java  # Scheduled email sender
│   ├── repository/                    # JPA data access layer
│   ├── model/                         # JPA entities
│   ├── dto/                           # Request & Response objects
│   ├── security/                      # JWT filter & provider
│   ├── exception/                     # Global error handling
│   └── scheduler/                     # @Scheduled tasks
├── src/test/                          # JUnit 5 + Mockito tests
└── pom.xml                            # Maven dependencies
```

### Frontend
```
hiretrack-frontend/
├── .github/workflows/ci.yml           # GitHub Actions CI pipeline
├── src/
│   ├── components/                    # Reusable React components
│   │   ├── dashboard/                 # StatsCard, FunnelChart
│   │   ├── applications/              # Table, Card, Form, StatusBadge
│   │   ├── skills/                    # SkillMatchScore, MissingSkills
│   │   └── ui/                        # Button, Modal, Input, Loader
│   ├── pages/                         # Next.js file-based routes
│   │   ├── index.jsx                  # Landing page
│   │   ├── dashboard.jsx              # Main hub
│   │   ├── applications/              # List, New, [id] detail
│   │   └── skills.jsx                 # Skills profile
│   ├── hooks/                         # Custom SWR data hooks
│   ├── lib/                           # Axios instance, auth helpers
│   └── store/                         # Jotai global state atoms
└── package.json
```

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=SkillMatcherServiceTest

# Run with coverage report (generates in target/site/jacoco)
mvn test jacoco:report
```

Tests cover:
- `SkillMatcherServiceTest` — match score calculation accuracy
- `ApplicationServiceTest` — CRUD operations and ownership validation
- `AuthServiceTest` — registration, login, duplicate email handling
- `ApplicationControllerTest` — endpoint security and response shapes

---

## 🌐 Deployment

### Backend → Render.com

1. Push your backend repo to GitHub
2. Go to [render.com](https://render.com) → New Web Service → Connect repo
3. Set build command: `mvn clean install -DskipTests`
4. Set start command: `java -jar target/hiretrack-0.0.1-SNAPSHOT.jar`
5. Add environment variables (same as application.properties)
6. Deploy — Render provides a free public URL

### Frontend → Vercel

1. Push your frontend repo to GitHub
2. Go to [vercel.com](https://vercel.com) → New Project → Import repo
3. Add environment variable: `NEXT_PUBLIC_API_URL=https://your-render-url.render.com`
4. Deploy — Vercel provides a free public URL instantly

### CI/CD Pipeline

Both repos include a GitHub Actions workflow that:
- Triggers on every push to `main` and every pull request
- Runs the full test suite automatically
- Blocks merges if tests fail

---

## 🗺 Roadmap

- [x] JWT Authentication (register / login / logout)
- [x] Job URL scraping with Jsoup
- [x] Skill gap analysis and match scoring
- [x] Application pipeline with 8 stages
- [x] Analytics dashboard
- [x] Automated email reminders via Spring Scheduler
- [x] Swagger API documentation
- [x] GitHub Actions CI/CD
- [ ] Resume version upload and storage (AWS S3)
- [ ] Browser extension — scrape job directly from LinkedIn page
- [ ] AI-powered resume tailoring suggestions (OpenAI API)
- [ ] Interview notes and rating system
- [ ] Export applications to CSV / PDF
- [ ] Docker + docker-compose for one-command local setup
- [ ] Mobile responsive PWA

---

## 👨‍💻 Author

<div align="center">

**Bhavya Mahyavanshi**

*Java Full-Stack Developer · Seneca Polytechnic · GPA 3.8*

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/bhavya-mahyavanshi)
[![Portfolio](https://img.shields.io/badge/Portfolio-Visit-0A0F2C?style=for-the-badge&logo=vercel&logoColor=white)](https://bhavya-mahyavanshi.vercel.app)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Bhavya-mahyavanshi)
[![Email](https://img.shields.io/badge/Email-Contact-EA4335?style=for-the-badge&logo=gmail&logoColor=white)](mailto:your@email.com)

</div>

---

<div align="center">

**Built with Java, Spring Boot, Next.js, and the determination to solve a real problem.**

If HireTrack helped you or you found it interesting — a ⭐ on GitHub means a lot.

</div>
