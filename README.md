# Hospital Management System

A comprehensive Spring Boot-based Hospital/Clinic Management System with JWT authentication, role-based access control, and RESTful APIs for managing patients, doctors, appointments, and billing.

## Documentation
- [Postman Collections](docs/postman/) - Pre-configured API collections for testing.
- [Frontend Integration Guide](FRONTEND_INTEGRATION_GUIDE.md) - Detailed guide for frontend developers.
- [Database Schema](docs/schema.md) - ER diagrams and table descriptions.

## Features

- 🔐 **JWT Authentication** - Secure login with token-based authentication
- 👥 **Role-Based Access Control** - Admin, Doctor, and Receptionist roles
- 🏥 **Patient Management** - CRUD operations with medical history tracking
- 👨‍⚕️ **Doctor Management** - Profiles, specializations, and availability scheduling
- 📅 **Appointment System** - Scheduling with status transitions
- 💰 **Billing System** - Invoice generation and payment tracking
- 📖 **API Documentation** - Swagger/OpenAPI integration
- 🐳 **Docker Ready** - Multi-stage Dockerfile included
- 🚀 **CI/CD Pipeline** - GitHub Actions workflow

## Tech Stack

- **Backend**: Spring Boot 3.2.1, Java 17
- **Security**: Spring Security, JWT (JJWT)
- **Database**: PostgreSQL 15
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5, JaCoCo
- **Containerization**: Docker, Docker Compose
- **CI/CD**: GitHub Actions

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- Docker & Docker Compose (optional)

## Quick Start

### Using Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repository-url>
cd HMS-project2

# Start all services
docker-compose up -d

# Access the application
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

### Manual Setup

1. **Configure Database**
```bash
# Create PostgreSQL database
createdb hms_db
```

2. **Set Environment Variables**
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=hms_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=yourSecretKey256BitsLong
```

3. **Build and Run**
```bash
mvn clean package
java -jar target/hospital-management-system-1.0.0.jar
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/refresh` | Refresh token |

### Patients
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/patients` | Get all patients | Admin, Doctor, Receptionist |
| GET | `/api/v1/patients/{id}` | Get patient by ID | Admin, Doctor, Receptionist |
| POST | `/api/v1/patients` | Create patient | Admin, Receptionist |
| PUT | `/api/v1/patients/{id}` | Update patient | Admin, Doctor, Receptionist |
| DELETE | `/api/v1/patients/{id}` | Delete patient | Admin |

### Doctors
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/doctors` | Get all doctors | All authenticated |
| GET | `/api/v1/doctors/{id}` | Get doctor by ID | All authenticated |
| POST | `/api/v1/doctors` | Create doctor | Admin |
| PUT | `/api/v1/doctors/{id}` | Update doctor | Admin, Own profile |
| DELETE | `/api/v1/doctors/{id}` | Delete doctor | Admin |

### Appointments
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/appointments` | Get all appointments | All authenticated |
| POST | `/api/v1/appointments` | Create appointment | All authenticated |
| PATCH | `/api/v1/appointments/{id}/status` | Update status | Admin, Doctor, Receptionist |

### Billing
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/billing/invoices` | Get all invoices | Admin, Receptionist |
| POST | `/api/v1/billing/invoices` | Create invoice | Admin, Receptionist |
| POST | `/api/v1/billing/payments` | Record payment | Admin, Receptionist |

## Project Structure

```
HMS-project2/
├── src/main/java/com/hms/
│   ├── config/          # Security, CORS, OpenAPI configs
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA entities
│   ├── enums/           # Role, Status enums
│   ├── exception/       # Custom exceptions, global handler
│   ├── repository/      # JPA repositories
│   ├── security/        # JWT service, filters
│   └── service/         # Business logic
├── src/main/resources/
│   ├── application.yml
│   └── application-prod.yml
├── Dockerfile
├── docker-compose.yml
└── .github/workflows/ci-cd.yml
```

## Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Deployment

### Docker Deployment

```bash
# Build image
docker build -t hms-app .

# Run container
docker run -d \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PASSWORD=yourpassword \
  -e JWT_SECRET=yourSecretKey \
  hms-app
```

### Health Checks

- Liveness: `GET /actuator/health/liveness`
- Readiness: `GET /actuator/health/readiness`
- Full Health: `GET /actuator/health`

## Team

- **Backend Team (4)**: Core APIs, Authentication, Testing/Deployment
- **Frontend Team (3)**: UI Components, API Integration
- **Project Manager (1)**: Coordination, Progress Tracking

## License

MIT License
# HospitalManagementSystem
