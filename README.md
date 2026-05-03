# 🔗 Bitly — URL Shortener API

A **production-grade URL Shortener API** built with Java Spring Boot, PostgreSQL, and Docker.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker)

---

## ✨ Features

- **URL Shortening** — Generate short codes (Base62) for any URL
- **Custom Aliases** — Optionally set your own short code
- **Redirect** — Access `/{shortCode}` to be redirected to the original URL
- **Click Analytics** — Track click count and last accessed timestamp
- **Expiration** — Set optional expiry dates for links
- **QR Code Generation** — Generate QR code PNGs for any shortened URL
- **Swagger UI** — Interactive API documentation at `/swagger-ui.html`
- **Dockerized** — Multi-stage Dockerfile + Docker Compose for one-command setup

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Spring Boot App                    │
│                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │Controller │→ │ Service  │→ │   Repository     │  │
│  │  (REST)   │  │ (Logic)  │  │ (JPA/Hibernate)  │  │
│  └──────────┘  └──────────┘  └──────────────────┘  │
│       ↑              ↑              ↓                │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │   DTOs   │  │  Utils   │  │   PostgreSQL     │  │
│  │          │  │ (Base62) │  │                    │  │
│  └──────────┘  └──────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### Folder Structure

```
Bitly/
├── docker-compose.yml          # App + PostgreSQL orchestration
├── Dockerfile                  # Multi-stage build
├── pom.xml                     # Maven dependencies
├── README.md
└── src/main/
    ├── java/com/bitly/
    │   ├── BitlyApplication.java       # Entry point
    │   ├── config/
    │   │   ├── CorsConfig.java         # CORS settings
    │   │   └── OpenApiConfig.java      # Swagger configuration
    │   ├── controller/
    │   │   ├── RedirectController.java # GET /{shortCode} → 302
    │   │   └── UrlController.java      # REST API endpoints
    │   ├── dto/
    │   │   ├── CreateUrlRequest.java   # Input validation
    │   │   ├── ErrorResponse.java      # Error format
    │   │   └── UrlResponse.java        # API response
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── ShortCodeAlreadyExistsException.java
    │   │   ├── UrlExpiredException.java
    │   │   └── UrlNotFoundException.java
    │   ├── model/
    │   │   └── UrlMapping.java         # JPA entity
    │   ├── repository/
    │   │   └── UrlMappingRepository.java
    │   ├── service/
    │   │   ├── QrCodeService.java      # ZXing QR generation
    │   │   └── UrlService.java         # Business logic
    │   └── util/
    │       └── Base62Encoder.java      # Short code generator
    └── resources/
        └── application.yml             # Configuration
```

---

## 🚀 Quick Start

### Prerequisites

- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- (Optional) Java 17+ & Maven 3.9+ for local development

### Run with Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repo-url> && cd Bitly

# Start everything (builds app + starts PostgreSQL)
docker-compose up --build

# App is available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

### Run Locally (Without Docker)

```bash
# Start a local PostgreSQL instance (or use Docker for DB only)
docker run -d --name bitly-db \
  -e POSTGRES_DB=bitly \
  -e POSTGRES_USER=bitly \
  -e POSTGRES_PASSWORD=bitly_secret \
  -p 5432:5432 \
  postgres:16-alpine

# Build and run the application
./mvnw spring-boot:run
```

### Stop & Clean Up

```bash
docker-compose down           # Stop containers
docker-compose down -v        # Stop + remove data volumes
```

---

## 📡 API Reference

### Base URL: `http://localhost:8080`

### 1. Create Short URL

```http
POST /api/urls
Content-Type: application/json

{
  "url": "https://www.example.com/very/long/path/to/resource",
  "expiresAt": "2026-12-31T23:59:59",
  "customAlias": "my-link"
}
```

**Response** `201 Created`:
```json
{
  "id": 1,
  "shortCode": "my-link",
  "originalUrl": "https://www.example.com/very/long/path/to/resource",
  "shortUrl": "http://localhost:8080/my-link",
  "clickCount": 0,
  "createdAt": "2026-05-03T10:30:00",
  "expiresAt": "2026-12-31T23:59:59"
}
```

### 2. Redirect (Browser)

```http
GET /{shortCode}
→ 302 Redirect to original URL
```

```bash
curl -L http://localhost:8080/my-link
# Redirects to https://www.example.com/very/long/path/to/resource
```

### 3. Get URL Stats

```http
GET /api/urls/{shortCode}
```

**Response** `200 OK`:
```json
{
  "id": 1,
  "shortCode": "my-link",
  "originalUrl": "https://www.example.com/very/long/path/to/resource",
  "shortUrl": "http://localhost:8080/my-link",
  "clickCount": 42,
  "createdAt": "2026-05-03T10:30:00",
  "lastAccessedAt": "2026-05-03T14:22:15",
  "expiresAt": "2026-12-31T23:59:59"
}
```

### 4. List All URLs

```http
GET /api/urls
```

### 5. Delete URL

```http
DELETE /api/urls/{shortCode}
→ 204 No Content
```

### 6. Generate QR Code

```http
GET /api/urls/{shortCode}/qr?width=300&height=300
→ image/png
```

```bash
# Save QR code to file
curl -o qr.png http://localhost:8080/api/urls/my-link/qr
```

---

## 🔍 Error Handling

All errors return a consistent JSON format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "URL with short code 'abc123' not found",
  "path": "/api/urls/abc123",
  "timestamp": "2026-05-03T10:30:00"
}
```

### Validation Errors (400)

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields have invalid values",
  "path": "/api/urls",
  "timestamp": "2026-05-03T10:30:00",
  "validationErrors": {
    "url": "Must be a valid URL",
    "expiresAt": "Expiration date must be in the future"
  }
}
```

| Status | Scenario |
|--------|----------|
| `201` | Short URL created |
| `200` | Stats retrieved |
| `204` | URL deleted |
| `302` | Redirect to original URL |
| `400` | Validation error |
| `404` | Short code not found |
| `409` | Custom alias already taken |
| `410` | URL has expired |
| `500` | Internal server error |

---

## 📖 Swagger / OpenAPI

Interactive documentation is auto-generated and available at:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## ⚙️ Configuration

All settings can be overridden via environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/bitly` | PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | `bitly` | Database username |
| `DATABASE_PASSWORD` | `bitly_secret` | Database password |
| `APP_BASE_URL` | `http://localhost:8080` | Base URL for generated short links |

---

## 🧪 Testing with cURL

```bash
# 1. Create a short URL
curl -s -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"url": "https://github.com"}' | jq

# 2. Create with custom alias + expiration
curl -s -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://docs.spring.io/spring-boot/docs/current/reference/html/",
    "customAlias": "spring",
    "expiresAt": "2027-01-01T00:00:00"
  }' | jq

# 3. Test redirect
curl -I http://localhost:8080/spring

# 4. Get stats
curl -s http://localhost:8080/api/urls/spring | jq

# 5. Download QR code
curl -o spring-qr.png http://localhost:8080/api/urls/spring/qr

# 6. List all URLs
curl -s http://localhost:8080/api/urls | jq

# 7. Delete a URL
curl -X DELETE http://localhost:8080/api/urls/spring
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.5 |
| **ORM** | Spring Data JPA / Hibernate |
| **Database** | PostgreSQL 16 |
| **Build Tool** | Maven |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **QR Codes** | Google ZXing |
| **Containerization** | Docker + Docker Compose |

---

## 📄 License

This project is licensed under the MIT License.
