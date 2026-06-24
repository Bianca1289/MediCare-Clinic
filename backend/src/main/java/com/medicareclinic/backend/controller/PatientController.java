package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.PatientResponse;
import com.medicareclinic.backend.dto.UpdatePatientRequest;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> myPatientRecord(Authentication authentication) {
        Patient p = patientService.getOrCreatePatientForUser(authentication.getName());
        return ResponseEntity.ok(patientService.toResponse(p));
    }

    @PutMapping("/me")
    public ResponseEntity<PatientResponse> updateMyPatientRecord(
            Authentication authentication,
            @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatientForUser(authentication.getName(), request));
    }
}
