package com.hms.controller;

import com.hms.dto.request.AppointmentRequest;
import com.hms.dto.request.AppointmentStatusRequest;
import com.hms.dto.response.ApiResponse;
import com.hms.dto.response.AppointmentResponse;
import com.hms.enums.AppointmentStatus;
import com.hms.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointments", description = "Appointment scheduling and management APIs")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "Get all appointments", description = "Retrieves list of all appointments")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAllAppointments() {
        log.info("GET /api/v1/appointments - Fetching all appointments");
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID", description = "Retrieves a specific appointment by ID")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable Long id) {
        log.info("GET /api/v1/appointments/{}", id);
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success(appointment));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get appointments by patient", description = "Retrieves all appointments for a patient")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByPatient(
            @PathVariable Long patientId) {
        log.info("GET /api/v1/appointments/patient/{}", patientId);
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get appointments by doctor", description = "Retrieves all appointments for a doctor")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByDoctor(
            @PathVariable Long doctorId) {
        log.info("GET /api/v1/appointments/doctor/{}", doctorId);
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get appointments by status", description = "Retrieves appointments by status")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByStatus(
            @PathVariable AppointmentStatus status) {
        log.info("GET /api/v1/appointments/status/{}", status);
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get appointments by date", description = "Retrieves all appointments for a specific date")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/v1/appointments/date/{}", date);
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @GetMapping("/doctor/{doctorId}/date/{date}")
    @Operation(summary = "Get doctor appointments by date", description = "Retrieves appointments for a doctor on a specific date")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDoctorAppointmentsByDate(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/v1/appointments/doctor/{}/date/{}", doctorId, date);
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointmentsByDate(doctorId, date);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @PostMapping
    @Operation(summary = "Create appointment", description = "Creates a new appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        log.info("POST /api/v1/appointments - Creating new appointment");
        AppointmentResponse appointment = appointmentService.createAppointment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment created successfully", appointment));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update appointment", description = "Updates an existing appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        log.info("PUT /api/v1/appointments/{}", id);
        AppointmentResponse appointment = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment updated successfully", appointment));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "Update appointment status", description = "Updates the status of an appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request) {
        log.info("PATCH /api/v1/appointments/{}/status", id);
        AppointmentResponse appointment = appointmentService.updateAppointmentStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated successfully", appointment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel appointment", description = "Cancels an existing appointment")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable Long id) {
        log.info("DELETE /api/v1/appointments/{}", id);
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully"));
    }
}
