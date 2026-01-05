package com.hms.service;

import com.hms.dto.request.MedicalHistoryRequest;
import com.hms.dto.request.PatientRequest;
import com.hms.dto.response.MedicalHistoryResponse;
import com.hms.dto.response.PatientResponse;
import com.hms.entity.MedicalHistory;
import com.hms.entity.Patient;
import com.hms.entity.User;
import com.hms.enums.Role;
import com.hms.exception.DuplicateResourceException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.MedicalHistoryRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public List<PatientResponse> getAllPatients() {
        log.info("Fetching all patients");
        return patientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PatientResponse getPatientById(Long id) {
        log.info("Fetching patient with ID: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        return mapToResponse(patient);
    }

    public List<PatientResponse> searchPatients(String name) {
        log.info("Searching patients by name: {}", name);
        return patientRepository.searchByName(name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        log.info("Creating new patient");

        // Check if email already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create user for patient
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_RECEPTIONIST); // Default role, can be changed as needed

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder
                        .encode(request.getPassword() != null ? request.getPassword() : "tempPassword123"))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(roles)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Create patient
        Patient patient = Patient.builder()
                .user(savedUser)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .address(request.getAddress())
                .emergencyContact(request.getEmergencyContact())
                .emergencyContactName(request.getEmergencyContactName())
                .build();

        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created with ID: {}", savedPatient.getId());

        return mapToResponse(savedPatient);
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        log.info("Updating patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        // Update user info
        User user = patient.getUser();
        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        userRepository.save(user);

        // Update patient info
        if (request.getDateOfBirth() != null)
            patient.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null)
            patient.setGender(request.getGender());
        if (request.getBloodGroup() != null)
            patient.setBloodGroup(request.getBloodGroup());
        if (request.getAddress() != null)
            patient.setAddress(request.getAddress());
        if (request.getEmergencyContact() != null)
            patient.setEmergencyContact(request.getEmergencyContact());
        if (request.getEmergencyContactName() != null)
            patient.setEmergencyContactName(request.getEmergencyContactName());

        Patient updatedPatient = patientRepository.save(patient);
        log.info("Patient updated successfully");

        return mapToResponse(updatedPatient);
    }

    @Transactional
    public void deletePatient(Long id) {
        log.info("Deleting patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        patientRepository.delete(patient);
        log.info("Patient deleted successfully");
    }

    // Medical History methods
    public List<MedicalHistoryResponse> getPatientMedicalHistory(Long patientId) {
        log.info("Fetching medical history for patient ID: {}", patientId);

        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", "id", patientId);
        }

        return medicalHistoryRepository.findByPatientIdOrderByDiagnosisDateDesc(patientId).stream()
                .map(this::mapToMedicalHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalHistoryResponse addMedicalHistory(Long patientId, MedicalHistoryRequest request) {
        log.info("Adding medical history for patient ID: {}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        MedicalHistory history = MedicalHistory.builder()
                .patient(patient)
                .conditionName(request.getConditionName())
                .treatment(request.getTreatment())
                .diagnosisDate(request.getDiagnosisDate())
                .notes(request.getNotes())
                .prescribedMedications(request.getPrescribedMedications())
                .attendingDoctor(request.getAttendingDoctor())
                .isChronic(request.getIsChronic())
                .build();

        MedicalHistory savedHistory = medicalHistoryRepository.save(history);
        log.info("Medical history added with ID: {}", savedHistory.getId());

        return mapToMedicalHistoryResponse(savedHistory);
    }

    @Transactional
    public MedicalHistoryResponse updateMedicalHistory(Long patientId, Long historyId, MedicalHistoryRequest request) {
        log.info("Updating medical history ID: {} for patient ID: {}", historyId, patientId);

        MedicalHistory history = medicalHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalHistory", "id", historyId));

        if (!history.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("MedicalHistory", "id", historyId);
        }

        if (request.getConditionName() != null)
            history.setConditionName(request.getConditionName());
        if (request.getTreatment() != null)
            history.setTreatment(request.getTreatment());
        if (request.getDiagnosisDate() != null)
            history.setDiagnosisDate(request.getDiagnosisDate());
        if (request.getNotes() != null)
            history.setNotes(request.getNotes());
        if (request.getPrescribedMedications() != null)
            history.setPrescribedMedications(request.getPrescribedMedications());
        if (request.getAttendingDoctor() != null)
            history.setAttendingDoctor(request.getAttendingDoctor());
        if (request.getIsChronic() != null)
            history.setIsChronic(request.getIsChronic());

        MedicalHistory updatedHistory = medicalHistoryRepository.save(history);
        log.info("Medical history updated successfully");

        return mapToMedicalHistoryResponse(updatedHistory);
    }

    @Transactional
    public void deleteMedicalHistory(Long patientId, Long historyId) {
        log.info("Deleting medical history ID: {} for patient ID: {}", historyId, patientId);

        MedicalHistory history = medicalHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalHistory", "id", historyId));

        if (!history.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("MedicalHistory", "id", historyId);
        }

        medicalHistoryRepository.delete(history);
        log.info("Medical history deleted successfully");
    }

    private PatientResponse mapToResponse(Patient patient) {
        User user = patient.getUser();
        return PatientResponse.builder()
                .id(patient.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .address(patient.getAddress())
                .emergencyContact(patient.getEmergencyContact())
                .emergencyContactName(patient.getEmergencyContactName())
                .createdAt(patient.getCreatedAt())
                .build();
    }

    private MedicalHistoryResponse mapToMedicalHistoryResponse(MedicalHistory history) {
        return MedicalHistoryResponse.builder()
                .id(history.getId())
                .patientId(history.getPatient().getId())
                .conditionName(history.getConditionName())
                .treatment(history.getTreatment())
                .diagnosisDate(history.getDiagnosisDate())
                .notes(history.getNotes())
                .prescribedMedications(history.getPrescribedMedications())
                .attendingDoctor(history.getAttendingDoctor())
                .isChronic(history.getIsChronic())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
