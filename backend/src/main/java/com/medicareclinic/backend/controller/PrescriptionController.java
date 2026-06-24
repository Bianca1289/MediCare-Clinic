package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.PrescriptionResponse;
import com.medicareclinic.backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/api/patient/prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PrescriptionResponse>> getMyPrescriptions(Authentication authentication) {
        return ResponseEntity.ok(prescriptionService.getForPatient(authentication.getName()));
    }
}
