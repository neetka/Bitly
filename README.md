# 🔗 Bitly — Enterprise-Grade URL Shortener & Analytics Platform

A **production-ready URL Shortener & Analytics Platform** built with **Java Spring Boot**, **PostgreSQL**, **Docker**, and a clean, responsive **Vanilla HTML/CSS/JS Single-Page Application (SPA)** dashboard.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/License-MIT-blueviolet?style=flat-square)

---

## ✨ Features & Capabilities

### 🔗 Link Management
*   **Base62 Shortening:** Automatically generate highly compact short codes using a robust custom Base62 encoding algorithm.
*   **Custom Brand Aliases:** Set your own meaningful custom alias instead of a randomized code (e.g., `/my-promo`).
*   **Optional Expirations:** Define an exact expiration date and time (`expiresAt`) after which the link becomes inactive.
*   **QR Code Generator:** Instant high-quality QR code generation (PNG) for any shortened link, ready to be embedded or downloaded.

### 🔒 Enterprise Security
*   **User Registration & Session Auth:** Full signup, login, session validation, and secure logout flow powered by **Spring Security** (session-based cookies).
*   **Link Ownership & Access Controls:** Users securely manage their own links. Links can be created anonymously or bound to an authenticated account.
*   **Password-Protected Links:** Lock sensitive links behind a password. Unauthenticated visitors are routed to a modern password-challenge interface to unlock the link.

### 📊 Rich Real-Time Analytics
*   **Click-Through Count:** Live track total redirect volume for every link.
*   **Detailed Event Logger:** Capture the precise timestamp, referring URL, client IP address (supporting proxy forwarding via `X-Forwarded-For`), and User-Agent details for every click.
*   **Smart Device Detection:** Automatic parser maps User-Agent metadata to device categories (**Desktop**, **Mobile**, **Tablet**, or **Unknown**).
*   **Search, Sort, and Filter:** Instantly filter your URL portfolio via custom query terms (`q`) and sort by creation date or click volume.

### ⚙️ System Monitoring
*   **Spring Boot Actuator:** Out-of-the-box system health (`/actuator/health`), metrics (`/actuator/metrics`), and info endpoints.
*   **Docker Health Checks:** Built-in automated container health monitoring.

### 🎨 Beautiful SPA Dashboard
*   A responsive, elegant dashboard built with modern CSS (custom properties, glassmorphism, flexbox/grid layout, and subtle micro-animations).
*   Interactive panels for User Auth, Link Creation (with advanced options toggle), full Link List/Search, Live QR Modals, and interactive Analytics Detail overlays.

---

## 🏗️ Architecture & Project Structure

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Spring Boot API & SPA                           │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                         Web/UI Layer                         │     │
│   │  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐  │     │
│   │  │  index.html  │  │ styles.css   │  │   password.html    │  │     │
│   │  └──────────────┘  └──────────────┘  └────────────────────┘  │     │
│   └──────────────────────────────┬───────────────────────────────┘     │
│                                  ▼                                     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                       Spring Boot REST                       │     │
│   │  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐  │     │
│   │  │  Auth API    │  │ Urls & Redir │  │ Analytics/Actuator │  │     │
│   │  └──────────────┘  └──────────────┘  └────────────────────┘  │     │
│   └──────────────────────────────┬───────────────────────────────┘     │
│                                  ▼                                     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                      Service Layer (Logic)                   │     │
│   │  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐  │     │
│   │  │ UserService  │  │  UrlService  │  │   QrCodeService    │  │     │
│   │  └──────────────┘  └──────────────┘  └────────────────────┘  │     │
│   └──────────────────────────────┬───────────────────────────────┘     │
│                                  ▼                                     │
│   ┌──────────────────────────────┴───────────────────────────────┐     │
│   │                    Repository (JPA & SQL)                    │     │
│   │               PostgreSQL Database (Docker Compose)           │     │
│   └──────────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────────────┘
```

### Folder Structure
```
Bitly/
├── docker-compose.yml          # Container orchestration (App + PostgreSQL)
├── Dockerfile                  # Multi-stage optimized JVM build
├── pom.xml                     # Maven configuration & dependencies
├── README.md                   # System documentation
└── src/main/
    ├── java/com/bitly/
    │   ├── BitlyApplication.java       # Application entry point
    │   ├── config/
    │   │   ├── CorsConfig.java         # CORS routing settings
    │   │   ├── OpenApiConfig.java      # OpenAPI 3/Swagger documentation setup
    │   │   └── SecurityConfig.java     # Spring Security authorization pipelines
    │   ├── controller/
    │   │   ├── AnalyticsController.java # Live deep-dive click logs
    │   │   ├── AuthController.java      # Account signup, session login, profiles
    │   │   ├── RedirectController.java # Public fast redirect resolver
    │   │   └── UrlController.java      # CRUD & secure resolution endpoints
    │   ├── dto/
    │   │   ├── AnalyticsResponse.java  # Analytical stats transfer DTO
    │   │   ├── AuthResponse.java       # User authentication transaction DTO
    │   │   ├── ClickEventResponse.java # Client details DTO
    │   │   ├── CreateUrlRequest.java   # Input parameters DTO
    │   │   ├── ErrorResponse.java      # Standardized system error body
    │   │   ├── LoginRequest.java       # Authentication credential parser
    │   │   ├── ResolvePasswordRequest.java # Password validation receiver
    │   │   ├── SignupRequest.java      # Registration input validations
    │   │   └── UrlResponse.java        # Standard short URL details response
    │   ├── exception/
    │   │   ├── AccessDeniedException.java
    │   │   ├── GlobalExceptionHandler.java  # Consolidated REST exception mapper
    │   │   ├── InvalidPasswordException.java
    │   │   ├── PasswordRequiredException.java
    │   │   ├── ShortCodeAlreadyExistsException.java
    │   │   ├── UrlExpiredException.java
    │   │   └── UrlNotFoundException.java
    │   ├── model/
    │   │   ├── ClickEvent.java         # JPA analytics log entity
    │   │   ├── UrlMapping.java         # JPA core shortened URL entity
    │   │   └── User.java               # JPA user account entity
    │   ├── repository/
    │   │   ├── ClickEventRepository.java
    │   │   ├── UrlMappingRepository.java
    │   │   └── UserRepository.java
    │   ├── service/
    │   │   ├── QrCodeService.java      # ZXing-based PNG generator
    │   │   ├── UrlService.java         # URL core logic, security, and tracking
    │   │   └── UserService.java        # User database operations and userDetailsService
    │   └── util/
    │       └── Base62Encoder.java      # High performance short-code generator
    └── resources/
        ├── application.yml             # Global spring environment settings
        ├── application-neon.yml        # Managed Neon DB profile override
        └── static/                     # Embedded SPA dashboard client
            ├── app.js                  # Frontend state machine & API client
            ├── index.html              # Main application web structure
            ├── password.html           # Target redirect challenge interface
            └── styles.css              # Custom styled sheets & design systems
```

---

## 🚀 Quick Start

### Prerequisites
*   [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
*   *(Optional)* Java 17+ & Maven 3.9+ for running directly on host

---

### Option A: Run with Docker Compose (Recommended)
This compiles the Spring Boot jar inside a builder container, packages it with an optimized runtime environment, spins up an isolated **PostgreSQL 16** server, and hooks them together instantly.

```bash
# 1. Clone or enter the workspace
cd Bitly

# 2. Spin up containers
docker-compose up --build
```
*   **Web Portal & Core App:** `http://localhost:8080`
*   **Interactive API Docs:** `http://localhost:8080/swagger-ui.html`

---

### Option B: Run Locally (Without Docker)
1. Ensure you have a running PostgreSQL database. Or run a clean database instance in the background using Docker:
   ```bash
   docker run -d --name bitly-db \
     -e POSTGRES_DB=bitly \
     -e POSTGRES_USER=bitly \
     -e POSTGRES_PASSWORD=bitly_secret \
     -p 5432:5432 \
     postgres:16-alpine
   ```

2. Compile and run the Spring Boot application using Maven:
   ```bash
   # Linux/macOS
   ./mvnw spring-boot:run
   
   # Windows
   mvnw.cmd spring-boot:run
   ```

---

## 📡 API Reference & Specifications

### 1. Authentication Services (`/api/auth`)

#### 📝 Register User
*   **Endpoint:** `POST /api/auth/signup`
*   **Payload:**
    ```json
    {
      "username": "developer101",
      "email": "dev@company.com",
      "password": "securePassword123"
    }
    ```
*   **Response (`201 Created`):**
    ```json
    {
      "username": "developer101",
      "email": "dev@company.com",
      "message": "Registration successful. You can now log in."
    }
    ```

#### 🔑 Log In
*   **Endpoint:** `POST /api/auth/login`
*   **Payload:**
    ```json
    {
      "username": "developer101",
      "password": "securePassword123"
    }
    ```
*   **Response (`200 OK` + Session Cookie `JSESSIONID`):**
    ```json
    {
      "username": "developer101",
      "email": "dev@company.com",
      "message": "Login successful"
    }
    ```

#### 👤 Current Profile Details
*   **Endpoint:** `GET /api/auth/me`
*   *Requires authenticated session cookie.*
*   **Response (`200 OK`):**
    ```json
    {
      "username": "developer101",
      "email": "dev@company.com"
    }
    ```

#### 🚪 Log Out
*   **Endpoint:** `POST /api/auth/logout`
*   **Response (`200 OK`):** clears session cookie.

---

### 2. Shortener Operations (`/api/urls`)

#### 🆕 Create Short Link
*   **Endpoint:** `POST /api/urls`
*   *Optional authenticated session. Authenticated users' links are automatically bound to their dashboard.*
*   **Payload:**
    ```json
    {
      "url": "https://docs.spring.io/spring-boot/index.html",
      "expiresAt": "2027-12-31T23:59:59",
      "customAlias": "spring-doc",
      "password": "mySuperSecretPassword"
    }
    ```
*   **Response (`201 Created`):**
    ```json
    {
      "id": 12,
      "shortCode": "spring-doc",
      "originalUrl": "https://docs.spring.io/spring-boot/index.html",
      "shortUrl": "http://localhost:8080/spring-doc",
      "clickCount": 0,
      "createdAt": "2026-05-22T12:00:00",
      "lastAccessedAt": null,
      "expiresAt": "2027-12-31T23:59:59",
      "passwordProtected": true
    }
    ```

#### 📋 List All Short Links
*   **Endpoint:** `GET /api/urls`
*   *Requires authenticated session.*
*   **Response (`200 OK`):** A JSON array of `UrlResponse` objects, ordered by creation date (newest first).

#### ℹ️ Get Link Details
*   **Endpoint:** `GET /api/urls/{shortCode}`
*   *Requires ownership of the short URL (or anonymous link creator).*
*   **Response (`200 OK`):** A single `UrlResponse` object representing the short link.

#### 🗑️ Delete Short Link
*   **Endpoint:** `DELETE /api/urls/{shortCode}`
*   *Requires ownership of the short URL.*
*   **Response (`204 No Content`):** Link successfully deleted.

#### 🔍 Search, Sort, and Filter Links
*   **Endpoint:** `GET /api/urls/search?q={query}&sortBy={field}&sortDir={asc|desc}`
*   *Requires authenticated session.*
*   *Example:* `GET /api/urls/search?q=spring&sortBy=clickCount&sortDir=desc`
*   **Response (`200 OK`):** A JSON array of matching `UrlResponse` objects.

#### 📊 Live Deep Analytics
*   **Endpoint:** `GET /api/urls/{shortCode}/analytics`
*   *Requires ownership of the short URL (or anonymous link creator).*
*   **Response (`200 OK`):**
    ```json
    {
      "shortCode": "spring-doc",
      "shortUrl": "http://localhost:8080/spring-doc",
      "originalUrl": "https://docs.spring.io/spring-boot/index.html",
      "totalClicks": 142,
      "recentClicks": [
        {
          "clickedAt": "2026-05-22T12:15:30",
          "referrer": "https://news.ycombinator.com/",
          "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15...",
          "deviceType": "Mobile",
          "ipAddress": "192.168.1.52"
        },
        {
          "clickedAt": "2026-05-22T12:04:10",
          "referrer": "Direct / None",
          "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)...",
          "deviceType": "Desktop",
          "ipAddress": "203.0.113.1"
        }
      ]
    }
    ```

#### 🛡️ Unlock Password-Protected Link
*   **Endpoint:** `POST /api/urls/{shortCode}/resolve`
*   **Payload:**
    ```json
    {
      "password": "mySuperSecretPassword"
    }
    ```
*   **Response (`200 OK`):**
    ```json
    {
      "originalUrl": "https://docs.spring.io/spring-boot/index.html"
    }
    ```

#### 🖼️ Fetch QR Code
*   **Endpoint:** `GET /api/urls/{shortCode}/qr?width=300&height=300`
*   **Response (`200 OK`):** Raw `image/png` bytes of the generated QR code.

---

### 3. Redirection Flow

#### ⚡ Get Redirect
*   **Endpoint:** `GET /{shortCode}`
*   *Public access.*
*   **Behavior:**
    *   **Normal Link:** Updates click metrics asynchronously, records device-specific metrics, and returns a standard `302 Found` status with `Location` header to trigger an instant redirection.
    *   **Password Protected Link:** Does not record a click. Responds with `302 Found` redirecting the user to `http://localhost:8080/password.html?code={shortCode}` where they must solve the password challenge. Once solved, their browser retrieves the original destination and finishes redirection.

---

## ⚙️ Configuration Properties

The system supports seamless scaling. Modify parameters via environment variables or inside `src/main/resources/application.yml`:

| Configuration Key | Env Override Variable | Default | Functional Description |
|---|---|---|---|
| `spring.datasource.url` | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/bitly` | Database connection URL |
| `spring.datasource.username` | `DATABASE_USERNAME` | `bitly` | PostgreSQL username |
| `spring.datasource.password` | `DATABASE_PASSWORD` | `bitly_secret` | PostgreSQL user password |
| `app.base-url` | `APP_BASE_URL` | `http://localhost:8080` | Prefix for generated shortened URL strings |
| `app.short-code-length` | `SHORT_CODE_LENGTH` | `7` | Default length for generated random codes |

---

## 🧪 Quick Test Guide (cURL)

Below is an interactive walk-through demonstrating all capabilities:

```bash
# Define local Cookie storage
COOKIE_JAR="./cookies.txt"

# 1. Register a new user
curl -s -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"neil_k","email":"neil@bitly.io","password":"mySecurityKey12"}' | jq

# 2. Log in programmatically & save session
curl -s -X POST http://localhost:8080/api/auth/login \
  -c "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -d '{"username":"neil_k","password":"mySecurityKey12"}' | jq

# 3. Create a normal, secure link locked behind a password
curl -s -X POST http://localhost:8080/api/urls \
  -b "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://github.com/features/actions",
    "customAlias": "gh-actions",
    "password": "lockAndKey101"
  }' | jq

# 4. Resolve the password challenge (unauthenticated access)
curl -s -X POST http://localhost:8080/api/urls/gh-actions/resolve \
  -H "Content-Type: application/json" \
  -d '{"password":"lockAndKey101"}' | jq

# 5. Retrieve deep click logs
curl -s -X GET http://localhost:8080/api/urls/gh-actions/analytics \
  -b "$COOKIE_JAR" | jq

# 6. Search for your spring-related codes
curl -s -X GET "http://localhost:8080/api/urls/search?q=gh&sortBy=createdAt&sortDir=desc" \
  -b "$COOKIE_JAR" | jq

# 7. Check current session status
curl -s -X GET http://localhost:8080/api/auth/me \
  -b "$COOKIE_JAR" | jq

# 8. Log out
curl -s -X POST http://localhost:8080/api/auth/logout \
  -b "$COOKIE_JAR"
```

---

## 🛠️ Complete Tech Stack

| Layer | Standard Technology |
|---|---|
| **Programming Language** | Java 17 (OpenJDK) |
| **Backend API Core** | Spring Boot 3.2.5 (Starter Web, Starter Validation) |
| **Code Generation** | Lombok |
| **Security Framework** | Spring Security 6 (Session-based, BCrypt hashing) |
| **Object-Relational Mapping (ORM)** | Spring Data JPA / Hibernate |
| **Relational Database** | PostgreSQL 16 |
| **Static Documentation** | SpringDoc OpenAPI UI / Swagger 3 |
| **QR Code Engine** | Google ZXing |
| **Compilation Tool** | Apache Maven |
| **Container Engine** | Docker & Compose Orchestrations |
| **User Interface** | Vanilla HTML5 / CSS3 (Aesthetic Dark Theme) / Modern ES6 JS |

---

## 📄 License

Distributed under the MIT License. See [LICENSE](LICENSE) for details.
