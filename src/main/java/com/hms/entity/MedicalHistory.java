package com.hms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank(message = "Condition/Diagnosis is required")
    @Column(name = "condition_name", nullable = false)
    private String conditionName;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "prescribed_medications", columnDefinition = "TEXT")
    private String prescribedMedications;

    @Column(name = "attending_doctor")
    private String attendingDoctor;

    @Column(name = "is_chronic")
    private Boolean isChronic;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
