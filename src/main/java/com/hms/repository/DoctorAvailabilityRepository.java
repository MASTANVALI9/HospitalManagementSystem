package com.hms.repository;

import com.hms.entity.DoctorAvailability;
import com.hms.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorId(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);

    List<DoctorAvailability> findByDoctorIdAndIsAvailable(Long doctorId, Boolean isAvailable);

    void deleteByDoctorId(Long doctorId);
}
