# PadelPal | Distributed Padel Court Booking Platform

A microservices-based backend system demonstrating distributed service design, inter-service communication, asynchronous messaging with RabbitMQ, and CI/CD automation.

---

## Team Members
- Petrec Matei-Teodor  
- Bontaș Andrian-Cosmin

---

# System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PadelPal System                                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐                   │
│  │ User Service │    │Court Service │    │  Booking Service │                   │
│  │   (8081)     │    │   (8082)     │    │     (8083)       │                   │
│  │              │    │              │    │                  │                   │
│  │  - CRUD      │    │  - CRUD      │    │  - CRUD          │                   │
│  │  - H2 DB     │    │  - H2 DB     │    │  - H2 DB         │                   │
│  └──────────────┘    └──────────────┘    │  - REST Client   │                   │
│         ▲                   ▲            │  - Event Publisher│                   │
│         │                   │            └────────┬─────────┘                   │
│         │    REST calls     │                     │                              │
│         └───────────────────┴─────────────────────┤                              │
│                                                   │ Publish                      │
│                                                   ▼                              │
│                                    ┌────────────────────────┐                   │
│                                    │       RabbitMQ         │                   │
│                                    │    Message Broker      │                   │
│                                    │   (5672 / 15672)       │                   │
│                                    │                        │                   │
│                                    │  Exchange: booking.    │                   │
│                                    │  Queue: booking.queue  │                   │
│                                    └───────────┬────────────┘                   │
│                                                │ Consume                         │
│                                                ▼                                 │
│                                    ┌────────────────────────┐                   │
│                                    │ Notification Service   │                   │
│                                    │       (8084)           │                   │
│                                    │                        │                   │
│                                    │  - Event Listener      │                   │
│                                    │  - H2 DB               │                   │
│                                    │  - Async Processing    │                   │
│                                    └────────────────────────┘                   │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Services Overview

| Service | Port | Description | Communication |
|---------|------|-------------|---------------|
| **User Service** | 8081 | Manages user registration and data | REST API |
| **Court Service** | 8082 | Manages padel court information | REST API |
| **Booking Service** | 8083 | Handles booking logic with validation | REST + RabbitMQ Publisher |
| **Notification Service** | 8084 | Processes booking notifications asynchronously | RabbitMQ Consumer |
| **RabbitMQ** | 5672/15672 | Message broker for async communication | AMQP |

---

# Message Queue Integration (RabbitMQ)

## Overview

RabbitMQ is integrated for **asynchronous communication** between the Booking Service and Notification Service. This demonstrates event-driven architecture in a microservices system.

## How It Works

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│ Booking Service │ ──────▶ │    RabbitMQ     │ ──────▶ │  Notification   │
│                 │ publish │                 │ consume │    Service      │
│  Creates booking│         │  booking.queue  │         │ Creates notif.  │
└─────────────────┘         └─────────────────┘         └─────────────────┘
        │                                                       │
        │ Immediate Response                     Async Processing
        ▼                                                       ▼
   "Booking Created"                              "Notification Saved"
```

### Event Flow

1. **Client** creates a booking via `POST /api/bookings?userId=1&courtId=1`
2. **Booking Service** validates user and court via REST calls
3. **Booking Service** saves the booking to its database
4. **Booking Service** publishes a `BookingEvent` to RabbitMQ
5. **Client** receives immediate response (booking confirmed)
6. **Notification Service** (listening to queue) receives the event asynchronously
7. **Notification Service** creates and saves a notification record

### Configuration Details

| Component | Value |
|-----------|-------|
| Exchange | `booking.exchange` (Topic) |
| Queue | `booking.queue` (Durable) |
| Routing Key | `booking.created` |
| Message Format | JSON |

## Benefits Demonstrated

### 1. **Decoupling**
- Booking Service doesn't need to know about Notification Service
- Services can evolve independently
- New consumers can be added without modifying the producer

### 2. **Scalability**
- Multiple Notification Service instances can consume from the same queue
- Competing consumers pattern distributes load
- Each service can scale independently based on its load

### 3. **Fault Tolerance**
- If Notification Service is down, messages remain in queue
- Messages are persisted (durable queue)
- When service restarts, it processes all pending messages
- Booking creation succeeds even if notification fails

### 4. **Async Processing**
- Booking response is immediate (no waiting for notification)
- Heavy processing happens in background
- Better user experience with faster response times

## RabbitMQ Management Console

Access at: **http://localhost:15672**
- Username: `guest`
- Password: `guest`

---

# CI/CD Pipeline (GitHub Actions)

## Overview

A complete CI/CD pipeline is configured using GitHub Actions that automatically builds, tests, and deploys all services.

## Pipeline Architecture

```
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│   Build & Test   │───▶│  Build Docker    │───▶│  Integration     │───▶│     Deploy       │
│                  │    │     Images       │    │     Tests        │    │                  │
│  • Maven build   │    │  • Build images  │    │  • Docker Compose│    │  • Deploy to     │
│  • Unit tests    │    │  • Tag with SHA  │    │  • Health checks │    │    local Docker  │
│  • All services  │    │  • All services  │    │  • E2E workflow  │    │  • Only on main  │
└──────────────────┘    └──────────────────┘    └──────────────────┘    └──────────────────┘
```

## Pipeline Stages

### Stage 1: Build and Test
- Runs for each service in parallel (matrix strategy)
- Compiles code with Maven
- Executes unit tests
- Uploads test reports as artifacts

### Stage 2: Build Docker Images
- Builds Docker images for all 4 services
- Tags images with commit SHA and `latest`
- Prepares for deployment

### Stage 3: Integration Tests
- Starts all services with Docker Compose
- Performs health checks on all endpoints
- Runs end-to-end workflow test:
  1. Create user
  2. Create court
  3. Create booking (triggers RabbitMQ)
  4. Verify notification was created

### Stage 4: Deploy
- Only runs on `main` or `master` branch
- Deploys to local Docker environment
- Provides service URLs

## Pipeline Triggers

```yaml
on:
  push:
    branches: [ main, master, develop ]
  pull_request:
    branches: [ main, master ]
```

## Viewing Pipeline Results

1. Go to your GitHub repository
2. Click on **"Actions"** tab
3. Select the workflow run to view details
4. Each job shows detailed logs

## Pipeline Configuration

The pipeline is defined in `.github/workflows/ci-cd.yml` and includes:

- **Parallel builds** for faster execution
- **Artifact uploads** for debugging
- **Health checks** with retry logic
- **Integration tests** with RabbitMQ verification
- **Automatic cleanup** after tests

---

# Deployment Instructions

## Prerequisites

- Docker Desktop installed and running
- Git installed
- Postman (for API testing)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd PadelPal-4-microservices-implementation
```

### 2. Build the Services

```bash
# Build all service JARs
cd user-service/user-service && mvn clean package -DskipTests && cd ../..
cd court-service/court-service && mvn clean package -DskipTests && cd ../..
cd booking-service/booking-service && mvn clean package -DskipTests && cd ../..
cd notification-service/notification-service && mvn clean package -DskipTests && cd ../..
```

Or use this one-liner (PowerShell):
```powershell
@("user-service","court-service","booking-service","notification-service") | ForEach-Object { Push-Location "$_/$_"; mvn clean package -DskipTests; Pop-Location }
```

### 3. Start All Services

```bash
docker-compose up --build
```

### 4. Verify Services Are Running

```bash
docker ps
```

Expected containers:
- `user-service` (8081)
- `court-service` (8082)
- `booking-service` (8083)
- `notification-service` (8084)
- `rabbitmq` (5672, 15672)

### 5. Access Services

| Service | URL |
|---------|-----|
| User Service | http://localhost:8081/api/users |
| Court Service | http://localhost:8082/api/courts |
| Booking Service | http://localhost:8083/api/bookings |
| Notification Service | http://localhost:8084/api/notifications |
| RabbitMQ Management | http://localhost:15672 |

---

# API Endpoints

## User Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create user (JSON body) |

## Court Service (Port 8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/courts` | Get all courts |
| GET | `/api/courts/{id}` | Get court by ID |
| POST | `/api/courts` | Create court (JSON body) |

## Booking Service (Port 8083)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bookings` | Get all bookings |
| GET | `/api/bookings/user/{userId}` | Get bookings by user |
| POST | `/api/bookings?userId={id}&courtId={id}` | Create booking |
| DELETE | `/api/bookings/{id}` | Delete booking |

## Notification Service (Port 8084)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications` | Get all notifications |
| GET | `/api/notifications/user/{userId}` | Get notifications by user |
| GET | `/api/notifications/booking/{bookingId}` | Get notifications by booking |
| GET | `/api/notifications/health` | Health check |

---

# Testing with Postman

## Import Collection

1. Open Postman
2. Click **Import**
3. Select `postman/PadelPal_Postman_Collection.json`

## Test Workflow (Demonstrating RabbitMQ)

Execute these requests in order to see the full async flow:

### Step 1: Create a User
```
POST http://localhost:8081/api/users
Body: {"name": "John Doe", "email": "john@example.com"}
```

### Step 2: Create a Court
```
POST http://localhost:8082/api/courts
Body: {"name": "Court A", "location": "Main Building", "indoor": true}
```

### Step 3: Create a Booking (Triggers RabbitMQ Event)
```
POST http://localhost:8083/api/bookings?userId=1&courtId=1
```
This publishes a message to RabbitMQ!

### Step 4: Check Notifications (Created Asynchronously)
```
GET http://localhost:8084/api/notifications
```
You should see the notification that was created by the Notification Service after consuming the RabbitMQ message!

---

# Development

## Local Development (Without Docker)

### 1. Start RabbitMQ
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. Run Services (Each in a separate terminal)
```bash
# Terminal 1
cd user-service/user-service && mvn spring-boot:run

# Terminal 2
cd court-service/court-service && mvn spring-boot:run

# Terminal 3
cd booking-service/booking-service && mvn spring-boot:run

# Terminal 4
cd notification-service/notification-service && mvn spring-boot:run
```

---

# Troubleshooting

## Services Not Starting

```bash
# Check logs
docker-compose logs <service-name>

# Restart services
docker-compose restart

# Full rebuild
docker-compose down -v
docker-compose up --build
```

## RabbitMQ Connection Issues

```bash
# Check RabbitMQ is running
docker-compose logs rabbitmq

# Access management console
# http://localhost:15672 → Queues tab
```

## Port Conflicts

```bash
# Check what's using the port (Windows)
netstat -ano | findstr :<port>

# Or on Linux/Mac
lsof -i :<port>
```

## Notification Not Appearing

- Wait 3-5 seconds after creating booking (async processing)
- Check Notification Service logs: `docker-compose logs notification-service`
- Verify RabbitMQ queue has consumers: http://localhost:15672 → Queues

---

# Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Architecture | Microservices |
| Sync Communication | REST APIs |
| Async Communication | RabbitMQ (AMQP) |
| Databases | H2 (in-memory, per service) |
| Build Tool | Maven |
| Containerization | Docker & Docker Compose |
| CI/CD | GitHub Actions |
| API Testing | Postman |

---

# Project Structure

```
PadelPal-4-microservices-implementation/
├── .github/
│   └── workflows/
│       └── ci-cd.yml              # GitHub Actions CI/CD pipeline
├── user-service/
│   └── user-service/
│       ├── src/main/java/...      # User management code
│       ├── Dockerfile
│       └── pom.xml
├── court-service/
│   └── court-service/
│       ├── src/main/java/...      # Court management code
│       ├── Dockerfile
│       └── pom.xml
├── booking-service/
│   └── booking-service/
│       ├── src/main/java/...      # Booking + RabbitMQ publisher
│       ├── Dockerfile
│       └── pom.xml
├── notification-service/
│   └── notification-service/
│       ├── src/main/java/...      # RabbitMQ consumer
│       ├── Dockerfile
│       └── pom.xml
├── postman/
│   └── PadelPal_Postman_Collection.json
├── docker-compose.yml             # Orchestrates all services + RabbitMQ
└── README.md
```

---

# Requirements Checklist

## Original Requirements
- [x] Three or more independent services (User, Court, Booking, Notification)
- [x] Inter-service REST communication (Booking → User/Court validation)
- [x] Postman collection covering all features
- [x] Docker setup with clear instructions

## Extended Requirements
- [x] **Message Queue Integration**
  - [x] RabbitMQ for async communication
  - [x] Booking Service publishes events
  - [x] Notification Service consumes events
  - [x] Benefits demonstrated (decoupling, scalability, fault tolerance)
  
- [x] **CI/CD Pipeline**
  - [x] GitHub Actions workflow
  - [x] Automatic build and test
  - [x] Docker image building
  - [x] Integration testing with Docker Compose
  - [x] Deployment to local Docker

- [x] **Documentation**
  - [x] Message queue architecture explained
  - [x] CI/CD pipeline configuration documented
  - [x] Clear instructions for running and observing

---
