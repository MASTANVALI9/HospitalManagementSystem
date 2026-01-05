package com.hms.repository;

import com.hms.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    Optional<Patient> findByUserEmail(String email);

    @Query("SELECT p FROM Patient p WHERE LOWER(p.user.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.user.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Patient> searchByName(@Param("name") String name);

    @Query("SELECT p FROM Patient p WHERE p.user.phone = :phone")
    Optional<Patient> findByPhone(@Param("phone") String phone);

    List<Patient> findByBloodGroup(String bloodGroup);
}
