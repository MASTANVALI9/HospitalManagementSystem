package com.hms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryResponse {

    private Long id;
    private Long patientId;
    private String conditionName;
    private String treatment;
    private LocalDate diagnosisDate;
    private String notes;
    private String prescribedMedications;
    private String attendingDoctor;
    private Boolean isChronic;
    private LocalDateTime createdAt;
}
