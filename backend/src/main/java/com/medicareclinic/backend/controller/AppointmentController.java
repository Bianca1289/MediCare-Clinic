package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.AppointmentResponse;
import com.medicareclinic.backend.dto.BookAppointmentRequest;
import com.medicareclinic.backend.dto.CreateAppointmentRequest;
import com.medicareclinic.backend.dto.RescheduleRequest;
import com.medicareclinic.backend.dto.UpdateAppointmentRequest;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.service.AppointmentService;
import com.medicareclinic.backend.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;

    // ── Receptionist ──────────────────────────────────────────────────────────

    @PostMapping("/api/receptionist/appointments")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request));
    }

    @GetMapping("/api/receptionist/appointments")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<Page<AppointmentResponse>> listAllAppointments(
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAllAppointmentsPaged(pageable));
    }

    @GetMapping("/api/receptionist/appointments/{id}")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @PutMapping("/api/receptionist/appointments/{id}")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, request));
    }

    @DeleteMapping("/api/receptionist/appointments/{id}")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Doctor ────────────────────────────────────────────────────────────────

    @GetMapping("/api/doctor/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AppointmentResponse>> doctorAppointments(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctorPaged(authentication.getName(), pageable));
    }

    @PutMapping("/api/doctor/appointments/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> doctorUpdateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, request));
    }

    // ── Patient ───────────────────────────────────────────────────────────────

    @GetMapping("/api/patient/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Page<AppointmentResponse>> patientAppointments(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForPatientUserPaged(authentication.getName(), pageable));
    }

    @PostMapping("/api/patient/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            Authentication authentication,
            @Valid @RequestBody BookAppointmentRequest request) {
        Patient patient = patientService.getPatientForUser(authentication.getName());
        CreateAppointmentRequest appointmentRequest = new CreateAppointmentRequest(
                patient.getId(), request.doctorUsername(), request.startTime(), request.notes());
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(appointmentRequest));
    }

    @PatchMapping("/api/patient/appointments/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(appointmentService.cancelForPatient(id, authentication.getName()));
    }

    @PatchMapping("/api/patient/appointments/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(appointmentService.rescheduleForPatient(id, request.startTime(), authentication.getName()));
    }
}
