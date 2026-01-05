package com.hms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryRequest {

    @NotBlank(message = "Condition name is required")
    private String conditionName;

    private String treatment;
    private LocalDate diagnosisDate;
    private String notes;
    private String prescribedMedications;
    private String attendingDoctor;
    private Boolean isChronic;
}
