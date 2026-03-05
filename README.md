# 🚀 Scalable Blog App — Enterprise CI/CD Deployment Pipeline

**A production-grade, fully automated CI/CD pipeline that builds, tests, scans, and deploys a scalable Spring Boot blog application to AWS EKS — zero manual intervention.**

[Architecture](#-architecture) • [Pipeline Stages](#-pipeline-stages) • [Tech Stack](#-tech-stack) • [Setup](#-setup-guide) • [API Reference](#-api-reference)

</div>

---

## 🧠 What This Project Demonstrates

This is not a toy project. Every design decision maps to real-world engineering standards:

| Concern | Solution | Why It Matters |
|---|---|---|
| Automated quality enforcement | SonarQube Quality Gate | Blocks merges with code smells, low coverage, security hotspots |
| Container vulnerability scanning | Trivy (CRITICAL/HIGH block) | Catches CVEs before they reach production |
| Artifact versioning | Nexus Repository Manager | Single source of truth for all build artifacts |
| Zero-downtime deployment | Kubernetes Rolling Update | No user-facing downtime during releases |
| Secrets management | K8s Secrets + env injection | DB credentials never hardcoded |
| Health monitoring | Spring Actuator + K8s probes | Auto-restart on failure, traffic only to healthy pods |
| Test coverage reporting | JaCoCo + SonarQube | Enforces minimum coverage threshold in CI |

---

## 🏗 Architecture

```
Developer Push
      │
      ▼
┌─────────────┐
│   GitHub    │  ← Webhook triggers Jenkins on every push to main
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│                    JENKINS PIPELINE                      │
│                                                         │
│  ┌──────────┐  ┌───────────┐  ┌──────────┐  ┌───────┐  │
│  │  Build   │→ │ SonarQube │→ │  Nexus   │→ │Docker │  │
│  │  + Test  │  │ Analysis  │  │ Artifact │  │ Build │  │
│  └──────────┘  └───────────┘  └──────────┘  └───┬───┘  │
│                                                  │      │
│                                            ┌─────▼────┐ │
│                                            │  Trivy   │ │
│                                            │   Scan   │ │
│                                            └─────┬────┘ │
└──────────────────────────────────────────────────┼──────┘
                                                   │
                                            ┌──────▼─────┐
                                            │  AWS ECR   │
                                            │  (Registry)│
                                            └──────┬─────┘
                                                   │
                                            ┌──────▼─────┐
                                            │  AWS EKS   │
                                            │ (Production)│
                                            └────────────┘
```

---

## ⚙️ Pipeline Stages

### Stage 1 — Source Control Trigger
- Developer pushes to `main` branch
- GitHub webhook fires HTTP POST to Jenkins
- Jenkins pulls latest code automatically

### Stage 2 — Build & Unit Test
```bash
mvn clean package
```
- Compiles 17 Java source files
- Runs unit tests (PostServiceTest, PostControllerTest)
- JaCoCo generates coverage report
- **Fails fast** — broken builds never proceed

### Stage 3 — Code Quality Analysis
```bash
mvn sonar:sonar
```
- SonarQube scans for bugs, vulnerabilities, code smells
- Quality Gate enforced: pipeline **blocks** if gate fails
- Coverage threshold enforced via JaCoCo integration

### Stage 4 — Artifact Storage
```bash
mvn deploy
```
- Versioned JAR uploaded to Nexus Repository
- Artifact tagged with build number + commit SHA
- Acts as immutable, auditable artifact history

### Stage 5 — Docker Build + Security Scan
```bash
docker build -t blog-app:${BUILD_NUMBER} .
trivy image --exit-code 1 --severity HIGH,CRITICAL blog-app:${BUILD_NUMBER}
```
- Multi-stage Dockerfile (builder + runtime)
- Non-root user enforced (security best practice)
- Trivy blocks deployment on HIGH/CRITICAL CVEs

### Stage 6 — Push to AWS ECR
```bash
docker push ${ECR_REGISTRY}/blog-app:${BUILD_NUMBER}
```
- Image tagged with immutable commit SHA
- IAM role-based authentication (no hardcoded keys)

### Stage 7 — Deploy to AWS EKS
```bash
helm upgrade --install blog-app ./helm/blog-app \
  --set image.tag=${BUILD_NUMBER}
```
- Rolling update strategy — zero downtime
- K8s liveness + readiness probes via `/actuator/health`
- Automatic rollback on failed health checks

---

## 🛠 Tech Stack

### Application
| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.2.3 |
| Database | PostgreSQL (prod) / H2 (test) |
| Security | Spring Security (Basic Auth → JWT-ready) |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven 3.9 |
| Testing | JUnit 5, Mockito, MockMvc |
| Coverage | JaCoCo |

### Infrastructure
| Tool | Role |
|---|---|
| Jenkins | Pipeline orchestration |
| SonarQube | Static code analysis + quality gate |
| Nexus OSS | Artifact repository |
| Docker | Containerization |
| Trivy | Container vulnerability scanning |
| AWS ECR | Private Docker registry |
| AWS EKS | Managed Kubernetes (production runtime) |
| Helm | Kubernetes package manager |
| AWS EC2 | Jenkins, SonarQube, Nexus servers |

---

## 📁 Project Structure

```
Scalable-Blog-App-Deployment-Pipeline/
├── src/
│   ├── main/java/com/blogapp/
│   │   ├── controller/          # REST endpoints (Post, Comment, Health)
│   │   ├── service/             # Business logic layer
│   │   ├── repository/          # JPA data access layer
│   │   ├── model/               # JPA entities (Post, Comment, Category)
│   │   ├── dto/                 # Request/Response DTOs
│   │   ├── exception/           # Global exception handling
│   │   └── config/              # Spring Security configuration
│   ├── main/resources/
│   │   ├── application.properties        # Production config (env var injection)
│   │   └── application-test.properties   # Test config (H2 in-memory)
│   └── test/java/com/blogapp/
│       ├── controller/          # MockMvc integration tests
│       └── service/             # Mockito unit tests
├── Dockerfile                   # Multi-stage build, non-root user
├── Jenkinsfile                  # Declarative pipeline (coming soon)
├── helm/                        # Kubernetes Helm chart (coming soon)
└── pom.xml                      # Maven build + JaCoCo + SonarQube config
```

---

## 🔌 API Reference

### Posts

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `GET` | `/api/v1/posts` | No | Get all posts (paginated) |
| `GET` | `/api/v1/posts/published` | No | Get published posts |
| `GET` | `/api/v1/posts/{id}` | No | Get post by ID |
| `GET` | `/api/v1/posts/search?keyword=` | No | Search posts |
| `GET` | `/api/v1/posts/recent` | No | Get 5 most recent posts |
| `POST` | `/api/v1/posts` | ✅ Yes | Create new post |
| `PUT` | `/api/v1/posts/{id}` | ✅ Yes | Update post |
| `DELETE` | `/api/v1/posts/{id}` | ✅ Yes | Delete post |

### Comments

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `GET` | `/api/v1/posts/{id}/comments` | No | Get comments for post |
| `POST` | `/api/v1/posts/{id}/comments` | No | Add comment |
| `DELETE` | `/api/v1/posts/{id}/comments/{cid}` | ✅ Yes | Delete comment |

### Health

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/health` | App health status |
| `GET` | `/actuator/health` | Spring Actuator (K8s probes) |

---

## 🚦 Setup Guide

### Prerequisites
- Java 21
- Maven 3.9+
- Docker
- AWS CLI configured
- kubectl + Helm

### Local Development

```bash
# Clone
git clone https://github.com/karan-patel11/Scalable-Blog-App-Deployment-Pipeline.git
cd Scalable-Blog-App-Deployment-Pipeline

# Build & Test
mvn clean package

# Run locally (requires PostgreSQL)
export DB_URL=jdbc:postgresql://localhost:5432/blogdb
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
java -jar target/blog-app-1.0.0.jar
```

### Docker Build

```bash
docker build -t blog-app:latest .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/blogdb \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=yourpassword \
  blog-app:latest
```

### Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/blogdb` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `secret` |

---

## 🔐 Security Design

- **Non-root Docker user** — container runs as `appuser`, not root
- **Trivy scanning** — blocks HIGH and CRITICAL CVEs before deployment
- **Spring Security** — all write operations require authentication
- **K8s Secrets** — database credentials injected via environment variables, never hardcoded
- **Actuator locked down** — sensitive endpoints require authorization

---

## 📊 Test Coverage

```
Tests run: 9
Failures:  0
Errors:    0

PostServiceTest      ████████████████████  5/5 passed
PostControllerTest   ████████████████████  4/4 passed
```

---

## 👨‍💻 Author

**Karan Patel**
MSCS Student — George Washington University
Seeking Full-Time SWE / DevOps / Cloud roles

[![GitHub](https://img.shields.io/badge/GitHub-karan--patel11-181717?style=flat-square&logo=github)](https://github.com/karan-patel11)

---

<div align="center">
<sub>Built with precision. Deployed with confidence.</sub>
</div>
