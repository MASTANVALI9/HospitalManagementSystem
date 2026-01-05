package com.hms.controller;

import com.hms.dto.request.MedicalHistoryRequest;
import com.hms.dto.request.PatientRequest;
import com.hms.dto.response.ApiResponse;
import com.hms.dto.response.MedicalHistoryResponse;
import com.hms.dto.response.PatientResponse;
import com.hms.service.PatientService;
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
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patients", description = "Patient management APIs")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "Get all patients", description = "Retrieves list of all patients")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAllPatients() {
        log.info("GET /api/v1/patients - Fetching all patients");
        List<PatientResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieves a specific patient by their ID")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        log.info("GET /api/v1/patients/{} - Fetching patient", id);
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @GetMapping("/search")
    @Operation(summary = "Search patients", description = "Search patients by name")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> searchPatients(
            @RequestParam String name) {
        log.info("GET /api/v1/patients/search?name={}", name);
        List<PatientResponse> patients = patientService.searchPatients(name);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Create patient", description = "Creates a new patient record")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @Valid @RequestBody PatientRequest request) {
        log.info("POST /api/v1/patients - Creating new patient");
        PatientResponse patient = patientService.createPatient(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient created successfully", patient));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient", description = "Updates an existing patient record")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        log.info("PUT /api/v1/patients/{} - Updating patient", id);
        PatientResponse patient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", patient));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete patient", description = "Deletes a patient record (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        log.info("DELETE /api/v1/patients/{} - Deleting patient", id);
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted successfully"));
    }

    // Medical History Endpoints
    @GetMapping("/{patientId}/medical-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get patient medical history", description = "Retrieves medical history for a patient")
    public ResponseEntity<ApiResponse<List<MedicalHistoryResponse>>> getMedicalHistory(
            @PathVariable Long patientId) {
        log.info("GET /api/v1/patients/{}/medical-history", patientId);
        List<MedicalHistoryResponse> history = patientService.getPatientMedicalHistory(patientId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PostMapping("/{patientId}/medical-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Add medical history", description = "Adds a new medical history record for a patient")
    public ResponseEntity<ApiResponse<MedicalHistoryResponse>> addMedicalHistory(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalHistoryRequest request) {
        log.info("POST /api/v1/patients/{}/medical-history", patientId);
        MedicalHistoryResponse history = patientService.addMedicalHistory(patientId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medical history added successfully", history));
    }

    @PutMapping("/{patientId}/medical-history/{historyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Update medical history", description = "Updates an existing medical history record")
    public ResponseEntity<ApiResponse<MedicalHistoryResponse>> updateMedicalHistory(
            @PathVariable Long patientId,
            @PathVariable Long historyId,
            @Valid @RequestBody MedicalHistoryRequest request) {
        log.info("PUT /api/v1/patients/{}/medical-history/{}", patientId, historyId);
        MedicalHistoryResponse history = patientService.updateMedicalHistory(patientId, historyId, request);
        return ResponseEntity.ok(ApiResponse.success("Medical history updated successfully", history));
    }

    @DeleteMapping("/{patientId}/medical-history/{historyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Delete medical history", description = "Deletes a medical history record")
    public ResponseEntity<ApiResponse<Void>> deleteMedicalHistory(
            @PathVariable Long patientId,
            @PathVariable Long historyId) {
        log.info("DELETE /api/v1/patients/{}/medical-history/{}", patientId, historyId);
        patientService.deleteMedicalHistory(patientId, historyId);
        return ResponseEntity.ok(ApiResponse.success("Medical history deleted successfully"));
    }
}
