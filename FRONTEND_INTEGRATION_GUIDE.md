# Hospital Management System (HMS) - Frontend Integration Guide

This document provides all the necessary information for the frontend team to integrate with the backend API.

## Project Information
- **Base URL**: `http://localhost:8080/api/v1`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/v3/api-docs`

---

## Authentication & Security

The system uses **JWT (JSON Web Token)** for authentication.

### Authentication Flow
1. **Login**: Send credentials to `/auth/login`.
2. **Receive Tokens**: The backend returns an `accessToken` and a `refreshToken`.
3. **Store Tokens**: Store the `accessToken` (e.g., in memory or state) and the `refreshToken` (e.g., in a secure cookie or localStorage).
4. **Authorize Requests**: Include the `accessToken` in the `Authorization` header of every subsequent request.
   - **Format**: `Authorization: Bearer <your_token>`

### Refreshing Tokens
When the `accessToken` expires, use the `refreshToken` to get a new one via `/auth/refresh`.

---

## Standard Response Format

All API responses follow a consistent structure:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2024-01-05T12:00:00",
  "errors": null
}
```

### Error Response
If a request fails (e.g., validation error), the response will look like this:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "timestamp": "2024-01-05T12:00:00",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

---

## User Roles
There are 4 main roles in the system:
- `ADMIN`: Full access to all modules.
- `DOCTOR`: Access to patient records, medical history, and their own appointments.
- `RECEPTIONIST`: Access to patient registration, appointment scheduling, and billing.
- `PATIENT`: Access to their own profile, medical history, and appointments.

---

## Endpoint Documentation

### 1. Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/register` | Create a new user account | Public |
| POST | `/login` | Authenticate and get tokens | Public |
| POST | `/refresh` | Get new access token using refresh token | Public |

**Login Request Example:**
```json
{
  "email": "admin@hms.com",
  "password": "password123"
}
```

---

### 2. Doctors (`/api/v1/doctors`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/` | List all doctors | Authenticated |
| GET | `/{id}` | Get doctor details | Authenticated |
| GET | `/available` | List currently available doctors | Authenticated |
| GET | `/specializations` | List all unique specializations | Authenticated |
| POST | `/` | Create a new doctor profile | `ADMIN` only |
| PUT | `/{id}` | Update doctor profile | `ADMIN` or Owner |
| PATCH | `/{id}/availability` | Toggle doctor availability | `ADMIN` or Owner |

---

### 3. Patients (`/api/v1/patients`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/` | List all patients | `ADMIN`, `DOCTOR`, `RECEPTIONIST` |
| GET | `/{id}` | Get patient details | Auth User (Role restricted) |
| GET | `/search` | Search patients by name | `ADMIN`, `DOCTOR`, `RECEPTIONIST` |
| POST | `/` | Register a new patient | `ADMIN`, `RECEPTIONIST` |
| GET | `/{id}/medical-history` | View medical history | `ADMIN`, `DOCTOR` |
| POST | `/{id}/medical-history` | Add medical history record | `ADMIN`, `DOCTOR` |

---

### 4. Appointments (`/api/v1/appointments`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/` | List all appointments | Authenticated |
| POST | `/` | Book a new appointment | Authenticated |
| GET | `/patient/{id}` | List patient appointments | Auth User |
| GET | `/doctor/{id}` | List doctor appointments | Auth User |
| PATCH | `/{id}/status` | Update status (CONFIRMED, CANCELLED, etc.) | `ADMIN`, `DOCTOR`, `RECEPTIONIST` |

---

### 5. Billing (`/api/v1/billing`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/invoices` | List all invoices | `ADMIN`, `RECEPTIONIST` |
| POST | `/invoices` | Create a new invoice | `ADMIN`, `RECEPTIONIST` |
| GET | `/invoices/patient/{id}` | Get patient invoices | `ADMIN`, `RECEPTIONIST` |
| POST | `/payments` | Record a payment for an invoice | `ADMIN`, `RECEPTIONIST` |

---

## Important Enumerations

### Appointment Status
- `PENDING`
- `CONFIRMED`
- `CANCELLED`
- `COMPLETED`
- `NOSHOW`

### Roles
- `ADMIN`
- `DOCTOR`
- `RECEPTIONIST`
- `PATIENT`

### Gender
- `MALE`
- `FEMALE`
- `OTHER`

---

## Development Tips
1. **Base Component**: Create a base API service/axios instance with the `Base URL` and interceptors for adding the `Authorization` header and handling token refresh on `401 Unauthorized` responses.
2. **Date Handling**: All dates are returned in ISO format (e.g., `2024-01-05`). Times for doctor availability are in `HH:mm` format.
3. **Feedback**: If you need a specific endpoint or a change in the data structure, please contact the backend team.
