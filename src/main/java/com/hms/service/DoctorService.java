package com.hms.service;

import com.hms.dto.request.DoctorRequest;
import com.hms.dto.response.DoctorResponse;
import com.hms.entity.Doctor;
import com.hms.entity.DoctorAvailability;
import com.hms.entity.User;
import com.hms.enums.Role;
import com.hms.exception.DuplicateResourceException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.DoctorAvailabilityRepository;
import com.hms.repository.DoctorRepository;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    public List<DoctorResponse> getAllDoctors() {
        log.info("Fetching all doctors");
        return doctorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DoctorResponse getDoctorById(Long id) {
        log.info("Fetching doctor with ID: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return mapToResponse(doctor);
    }

    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        log.info("Fetching doctors by specialization: {}", specialization);
        return doctorRepository.findBySpecialization(specialization).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DoctorResponse> getAvailableDoctors() {
        log.info("Fetching available doctors");
        return doctorRepository.findByIsAvailable(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<String> getAllSpecializations() {
        return doctorRepository.findAllSpecializations();
    }

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        log.info("Creating new doctor");

        // Check if email already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Check if license number already exists
        if (doctorRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new DuplicateResourceException("Doctor", "licenseNumber", request.getLicenseNumber());
        }

        // Create user for doctor
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_DOCTOR);

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

        // Create doctor
        Doctor doctor = Doctor.builder()
                .user(savedUser)
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .consultationFee(request.getConsultationFee())
                .qualification(request.getQualification())
                .yearsOfExperience(request.getYearsOfExperience())
                .bio(request.getBio())
                .isAvailable(true)
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);

        // Add availability if provided
        if (request.getAvailabilities() != null && !request.getAvailabilities().isEmpty()) {
            for (DoctorRequest.AvailabilitySlot slot : request.getAvailabilities()) {
                DoctorAvailability availability = DoctorAvailability.builder()
                        .doctor(savedDoctor)
                        .dayOfWeek(slot.getDayOfWeek())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .isAvailable(true)
                        .build();
                availabilityRepository.save(availability);
            }
        }

        log.info("Doctor created with ID: {}", savedDoctor.getId());
        return mapToResponse(doctorRepository.findById(savedDoctor.getId()).get());
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, DoctorRequest request) {
        log.info("Updating doctor with ID: {}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));

        // Update user info
        User user = doctor.getUser();
        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        userRepository.save(user);

        // Update doctor info
        if (request.getSpecialization() != null)
            doctor.setSpecialization(request.getSpecialization());
        if (request.getConsultationFee() != null)
            doctor.setConsultationFee(request.getConsultationFee());
        if (request.getQualification() != null)
            doctor.setQualification(request.getQualification());
        if (request.getYearsOfExperience() != null)
            doctor.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getBio() != null)
            doctor.setBio(request.getBio());

        // Update availability if provided
        if (request.getAvailabilities() != null) {
            availabilityRepository.deleteByDoctorId(id);
            for (DoctorRequest.AvailabilitySlot slot : request.getAvailabilities()) {
                DoctorAvailability availability = DoctorAvailability.builder()
                        .doctor(doctor)
                        .dayOfWeek(slot.getDayOfWeek())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .isAvailable(true)
                        .build();
                availabilityRepository.save(availability);
            }
        }

        Doctor updatedDoctor = doctorRepository.save(doctor);
        log.info("Doctor updated successfully");

        return mapToResponse(updatedDoctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        log.info("Deleting doctor with ID: {}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));

        doctorRepository.delete(doctor);
        log.info("Doctor deleted successfully");
    }

    @Transactional
    public DoctorResponse setDoctorAvailability(Long id, boolean isAvailable) {
        log.info("Setting doctor {} availability to {}", id, isAvailable);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));

        doctor.setIsAvailable(isAvailable);
        Doctor updatedDoctor = doctorRepository.save(doctor);

        return mapToResponse(updatedDoctor);
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        User user = doctor.getUser();

        List<DoctorResponse.AvailabilitySlot> availabilitySlots = doctor.getAvailabilities().stream()
                .map(a -> DoctorResponse.AvailabilitySlot.builder()
                        .id(a.getId())
                        .dayOfWeek(a.getDayOfWeek())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .isAvailable(a.getIsAvailable())
                        .build())
                .collect(Collectors.toList());

        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .specialization(doctor.getSpecialization())
                .licenseNumber(doctor.getLicenseNumber())
                .consultationFee(doctor.getConsultationFee())
                .qualification(doctor.getQualification())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .bio(doctor.getBio())
                .isAvailable(doctor.getIsAvailable())
                .createdAt(doctor.getCreatedAt())
                .availabilities(availabilitySlots)
                .build();
    }
}
