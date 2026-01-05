package com.hms.controller;

import com.hms.dto.request.DoctorRequest;
import com.hms.dto.response.ApiResponse;
import com.hms.dto.response.DoctorResponse;
import com.hms.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Doctors", description = "Doctor management APIs")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieves list of all doctors")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctors() {
        log.info("GET /api/v1/doctors - Fetching all doctors");
        List<DoctorResponse> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieves a specific doctor by their ID")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(@PathVariable Long id) {
        log.info("GET /api/v1/doctors/{} - Fetching doctor", id);
        DoctorResponse doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success(doctor));
    }

    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "Get doctors by specialization", description = "Retrieves doctors by their specialization")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getDoctorsBySpecialization(
            @PathVariable String specialization) {
        log.info("GET /api/v1/doctors/specialization/{}", specialization);
        List<DoctorResponse> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available doctors", description = "Retrieves all currently available doctors")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAvailableDoctors() {
        log.info("GET /api/v1/doctors/available");
        List<DoctorResponse> doctors = doctorService.getAvailableDoctors();
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @GetMapping("/specializations")
    @Operation(summary = "Get all specializations", description = "Retrieves list of all doctor specializations")
    public ResponseEntity<ApiResponse<List<String>>> getAllSpecializations() {
        log.info("GET /api/v1/doctors/specializations");
        List<String> specializations = doctorService.getAllSpecializations();
        return ResponseEntity.ok(ApiResponse.success(specializations));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create doctor", description = "Creates a new doctor profile (Admin only)")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(
            @Valid @RequestBody DoctorRequest request) {
        log.info("POST /api/v1/doctors - Creating new doctor");
        DoctorResponse doctor = doctorService.createDoctor(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor created successfully", doctor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @doctorService.getDoctorById(#id).userId == authentication.principal.id")
    @Operation(summary = "Update doctor", description = "Updates an existing doctor profile")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {
        log.info("PUT /api/v1/doctors/{} - Updating doctor", id);
        DoctorResponse doctor = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", doctor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete doctor", description = "Deletes a doctor profile (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        log.info("DELETE /api/v1/doctors/{} - Deleting doctor", id);
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor deleted successfully"));
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN') or @doctorService.getDoctorById(#id).userId == authentication.principal.id")
    @Operation(summary = "Set doctor availability", description = "Sets doctor availability status")
    public ResponseEntity<ApiResponse<DoctorResponse>> setDoctorAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        log.info("PATCH /api/v1/doctors/{}/availability?available={}", id, available);
        DoctorResponse doctor = doctorService.setDoctorAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success("Doctor availability updated", doctor));
    }
}
