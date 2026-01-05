package com.hms.service;

import com.hms.dto.request.AppointmentRequest;
import com.hms.dto.request.AppointmentStatusRequest;
import com.hms.dto.response.AppointmentResponse;
import com.hms.entity.Appointment;
import com.hms.entity.Doctor;
import com.hms.entity.Patient;
import com.hms.enums.AppointmentStatus;
import com.hms.exception.BadRequestException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public List<AppointmentResponse> getAllAppointments() {
        log.info("Fetching all appointments");
        return appointmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse getAppointmentById(Long id) {
        log.info("Fetching appointment with ID: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        return mapToResponse(appointment);
    }

    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        log.info("Fetching appointments for patient ID: {}", patientId);
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        log.info("Fetching appointments for doctor ID: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status) {
        log.info("Fetching appointments by status: {}", status);
        return appointmentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByDate(LocalDate date) {
        log.info("Fetching appointments for date: {}", date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return appointmentRepository.findByDateRange(startOfDay, endOfDay).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getDoctorAppointmentsByDate(Long doctorId, LocalDate date) {
        log.info("Fetching appointments for doctor {} on date: {}", doctorId, date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return appointmentRepository.findByDoctorIdAndDateRange(doctorId, startOfDay, endOfDay).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        log.info("Creating new appointment");

        // Validate patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        // Check if doctor is available
        if (!doctor.getIsAvailable()) {
            throw new BadRequestException("Doctor is not available for appointments");
        }

        // Check for conflicting appointments
        if (appointmentRepository.existsConflictingAppointment(request.getDoctorId(), request.getAppointmentTime())) {
            throw new BadRequestException("Doctor already has an appointment at this time");
        }

        // Validate appointment time is in the future
        if (request.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment time must be in the future");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.PENDING)
                .reason(request.getReason())
                .notes(request.getNotes())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created with ID: {}", savedAppointment.getId());

        return mapToResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        log.info("Updating appointment with ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        // Can only update pending or confirmed appointments
        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot update completed or cancelled appointments");
        }

        // Update appointment time if provided
        if (request.getAppointmentTime() != null) {
            if (request.getAppointmentTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Appointment time must be in the future");
            }

            // Check for conflicts (excluding current appointment)
            if (!request.getAppointmentTime().equals(appointment.getAppointmentTime())) {
                if (appointmentRepository.existsConflictingAppointment(
                        appointment.getDoctor().getId(), request.getAppointmentTime())) {
                    throw new BadRequestException("Doctor already has an appointment at this time");
                }
            }
            appointment.setAppointmentTime(request.getAppointmentTime());
        }

        if (request.getReason() != null)
            appointment.setReason(request.getReason());
        if (request.getNotes() != null)
            appointment.setNotes(request.getNotes());
        if (request.getDurationMinutes() != null)
            appointment.setDurationMinutes(request.getDurationMinutes());

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment updated successfully");

        return mapToResponse(updatedAppointment);
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatusRequest request) {
        log.info("Updating status for appointment ID: {} to {}", id, request.getStatus());

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        // Validate status transition
        if (!appointment.canTransitionTo(request.getStatus())) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s",
                            appointment.getStatus(), request.getStatus()));
        }

        appointment.setStatus(request.getStatus());
        if (request.getDoctorNotes() != null) {
            appointment.setDoctorNotes(request.getDoctorNotes());
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment status updated successfully");

        return mapToResponse(updatedAppointment);
    }

    @Transactional
    public void cancelAppointment(Long id) {
        log.info("Cancelling appointment with ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        if (!appointment.canTransitionTo(AppointmentStatus.CANCELLED)) {
            throw new BadRequestException("Cannot cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        log.info("Appointment cancelled successfully");
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patient(AppointmentResponse.PatientSummary.builder()
                        .id(patient.getId())
                        .firstName(patient.getUser().getFirstName())
                        .lastName(patient.getUser().getLastName())
                        .phone(patient.getUser().getPhone())
                        .build())
                .doctor(AppointmentResponse.DoctorSummary.builder()
                        .id(doctor.getId())
                        .firstName(doctor.getUser().getFirstName())
                        .lastName(doctor.getUser().getLastName())
                        .specialization(doctor.getSpecialization())
                        .build())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .doctorNotes(appointment.getDoctorNotes())
                .durationMinutes(appointment.getDurationMinutes())
                .createdAt(appointment.getCreatedAt())
                .invoiceId(appointment.getInvoice() != null ? appointment.getInvoice().getId() : null)
                .build();
    }
}
