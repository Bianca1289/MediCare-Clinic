package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.AppointmentResponse;
import com.medicareclinic.backend.dto.BookAppointmentRequest;
import com.medicareclinic.backend.dto.CreateAppointmentRequest;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.service.AppointmentService;
import com.medicareclinic.backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;

    // Receptionist creates appointments
    @PostMapping("/api/receptionist/appointments")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.createAppointment(request));
    }

    // Doctor views their appointments
    @GetMapping("/api/doctor/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> doctorAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(authentication.getName()));
    }

    // Patient views their appointments
    @GetMapping("/api/patient/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> patientAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForPatientUser(authentication.getName()));
    }

    // Patient creates their own appointment
    @PostMapping("/api/patient/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(Authentication authentication, @Valid @RequestBody BookAppointmentRequest request) {
        // Get the patient record for this user
        Patient patient = patientService.getPatientForUser(authentication.getName());

        // Create appointment request
        CreateAppointmentRequest appointmentRequest = new CreateAppointmentRequest(patient.getId(), request.doctorUsername(), request.startTime(), request.notes());
        AppointmentResponse response = appointmentService.createAppointment(appointmentRequest);

        return ResponseEntity.ok(response);
    }
}

