package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.AppointmentResponse;
import com.medicareclinic.backend.dto.CreateAppointmentRequest;
import com.medicareclinic.backend.model.Appointment;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.AppointmentRepository;
import com.medicareclinic.backend.repository.PatientRepository;
import com.medicareclinic.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    //private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository, UserRepository userRepository){//, EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        //this.emailService = emailService;
    }

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient not found: " + request.patientId()));

        User doctor = userRepository.findByUsername(request.doctorUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor user not found: " + request.doctorUsername()));

        LocalDateTime start;
        try {
            start = LocalDateTime.parse(request.startTime());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid startTime format, expected ISO-8601");
        }

        // Check for conflicts: no overlapping appointments for this doctor within 1 hour
        checkForConflicts(doctor.getId(), start);

        Appointment ap = new Appointment();
        ap.setPatient(patient);
        ap.setDoctor(doctor);
        ap.setStartTime(start);
        ap.setStatus("SCHEDULED");
        ap.setNotes(request.notes());

        Appointment saved = appointmentRepository.save(ap);

//        // Send confirmation email asynchronously
//        String patientEmail = patient.getEmail();
//        if (patientEmail != null && !patientEmail.isBlank()) {
//            emailService.sendAppointmentConfirmation(patientEmail, patient.getFullName(), doctor.getUsername(), start, saved.getId());
//        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsForDoctor(String doctorUsername) {
        return appointmentRepository.findByDoctor_Username(doctorUsername).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsForPatientUser(String patientUsername) {
        return appointmentRepository.findByPatient_User_Username(patientUsername).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void checkForConflicts(Long doctorId, LocalDateTime appointmentStart) {
        LocalDateTime windowStart = appointmentStart.minusMinutes(30);
        LocalDateTime windowEnd = appointmentStart.plusMinutes(90);

        List<Appointment> conflicts = appointmentRepository.findByDoctor_Id(doctorId).stream()
                .filter(a -> "SCHEDULED".equals(a.getStatus()))
                .filter(a -> !a.getStartTime().isAfter(windowEnd) && (a.getStartTime()).isBefore(windowStart.plusMinutes(30)))
                .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor has a conflict: appointment already scheduled in this time window");
        }
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getDoctor().getUsername(),
                a.getStartTime(),
                a.getStatus(),
                a.getNotes()
        );
    }
}

