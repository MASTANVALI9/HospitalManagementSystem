package com.hms.service;

import com.hms.dto.request.AppointmentRequest;
import com.hms.dto.request.AppointmentStatusRequest;
import com.hms.dto.response.AppointmentResponse;
import com.hms.entity.Appointment;
import com.hms.entity.Doctor;
import com.hms.entity.Patient;
import com.hms.entity.User;
import com.hms.enums.AppointmentStatus;
import com.hms.exception.BadRequestException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private User patientUser;
    private User doctorUser;

    @BeforeEach
    void setUp() {
        patientUser = User.builder()
                .id(1L)
                .email("patient@test.com")
                .firstName("Jane")
                .lastName("Doe")
                .phone("1234567890")
                .build();

        doctorUser = User.builder()
                .id(2L)
                .email("doctor@test.com")
                .firstName("Dr. John")
                .lastName("Smith")
                .build();

        testPatient = Patient.builder()
                .id(1L)
                .user(patientUser)
                .build();

        testDoctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .specialization("Cardiology")
                .isAvailable(true)
                .build();

        testAppointment = Appointment.builder()
                .id(1L)
                .patient(testPatient)
                .doctor(testDoctor)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.PENDING)
                .reason("Checkup")
                .durationMinutes(30)
                .build();
    }

    @Test
    @DisplayName("Should get all appointments")
    void getAllAppointments_Success() {
        // Given
        when(appointmentRepository.findAll()).thenReturn(Arrays.asList(testAppointment));

        // When
        List<AppointmentResponse> result = appointmentService.getAllAppointments();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should get appointment by ID")
    void getAppointmentById_Success() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        // When
        AppointmentResponse result = appointmentService.getAppointmentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    }

    @Test
    @DisplayName("Should throw exception when appointment not found")
    void getAppointmentById_NotFound_ThrowsException() {
        // Given
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> appointmentService.getAppointmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should create appointment successfully")
    void createAppointment_Success() {
        // Given
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .reason("Checkup")
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsConflictingAppointment(anyLong(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        AppointmentResponse result = appointmentService.createAppointment(request);

        // Then
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when doctor is not available")
    void createAppointment_DoctorUnavailable_ThrowsException() {
        // Given
        testDoctor.setIsAvailable(false);
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("Should throw exception for conflicting appointment")
    void createAppointment_Conflict_ThrowsException() {
        // Given
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsConflictingAppointment(anyLong(), any())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already has an appointment");
    }

    @Test
    @DisplayName("Should throw exception for past appointment time")
    void createAppointment_PastTime_ThrowsException() {
        // Given
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .appointmentTime(LocalDateTime.now().minusDays(1))
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future");
    }

    @Test
    @DisplayName("Should update appointment status successfully")
    void updateAppointmentStatus_Success() {
        // Given
        AppointmentStatusRequest request = AppointmentStatusRequest.builder()
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        AppointmentResponse result = appointmentService.updateAppointmentStatus(1L, request);

        // Then
        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid status transition")
    void updateAppointmentStatus_InvalidTransition_ThrowsException() {
        // Given
        testAppointment.setStatus(AppointmentStatus.COMPLETED);
        AppointmentStatusRequest request = AppointmentStatusRequest.builder()
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        // When/Then
        assertThatThrownBy(() -> appointmentService.updateAppointmentStatus(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transition");
    }

    @Test
    @DisplayName("Should cancel appointment successfully")
    void cancelAppointment_Success() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        appointmentService.cancelAppointment(1L);

        // Then
        verify(appointmentRepository).save(any(Appointment.class));
    }
}
