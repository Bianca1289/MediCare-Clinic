package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.PatientResponse;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> myPatientRecord(Authentication authentication) {
        Patient p = patientService.getPatientForUser(authentication.getName());
        String username = p.getUser() != null ? p.getUser().getUsername() : null;
        return ResponseEntity.ok(new PatientResponse(p.getId(), p.getFullName(), p.getContactInfo(), p.getEmail(), username));
    }
}

