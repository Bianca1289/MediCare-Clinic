package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.CreatePatientRequest;
import com.medicareclinic.backend.dto.PatientResponse;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/receptionist/patients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionistPatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        Patient p = patientService.createPatient(request);
        return ResponseEntity.ok(toResponse(p));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> listPatients() {
        List<PatientResponse> list = patientService.getAllPatients().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    private PatientResponse toResponse(Patient p) {
        String username = p.getUser() != null ? p.getUser().getUsername() : null;
        return new PatientResponse(p.getId(), p.getFullName(), p.getContactInfo(), p.getEmail(), username);
    }
}

