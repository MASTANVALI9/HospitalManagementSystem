package com.hms.repository;

import com.hms.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

    List<MedicalHistory> findByPatientId(Long patientId);

    List<MedicalHistory> findByPatientIdOrderByDiagnosisDateDesc(Long patientId);

    List<MedicalHistory> findByIsChronic(Boolean isChronic);

    List<MedicalHistory> findByPatientIdAndIsChronic(Long patientId, Boolean isChronic);
}
