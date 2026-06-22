package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.CreatePatientRequest;
import com.medicareclinic.backend.dto.PatientResponse;
import com.medicareclinic.backend.dto.UpdatePatientRequest;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receptionist/patients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionistPatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        Patient p = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.toResponse(p));
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> listPatients(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(patientService.searchPatients(search, pageable));
        }
        return ResponseEntity.ok(patientService.getAllPatientsPaged(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable Long id) {
        Patient p = patientService.getById(id);
        return ResponseEntity.ok(patientService.toResponse(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
