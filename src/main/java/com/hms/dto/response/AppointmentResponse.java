package com.hms.dto.response;

import com.hms.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private PatientSummary patient;
    private DoctorSummary doctor;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
    private String reason;
    private String notes;
    private String doctorNotes;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
    private Long invoiceId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String specialization;
    }
}
