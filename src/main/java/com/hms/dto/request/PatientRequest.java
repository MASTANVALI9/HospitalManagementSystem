package com.hms.dto.request;

import com.hms.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {

    // User info (for new patient registration)
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;

    // Patient specific info
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private String bloodGroup;
    private String address;
    private String emergencyContact;
    private String emergencyContactName;
}
