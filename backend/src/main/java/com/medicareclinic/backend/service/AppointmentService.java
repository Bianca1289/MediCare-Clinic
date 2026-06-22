package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.AppointmentResponse;
import com.medicareclinic.backend.dto.CreateAppointmentRequest;
import com.medicareclinic.backend.dto.UpdateAppointmentRequest;
import com.medicareclinic.backend.model.Appointment;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.AppointmentRepository;
import com.medicareclinic.backend.repository.PatientRepository;
import com.medicareclinic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

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

        checkForConflicts(doctor.getId(), start);

        Appointment ap = new Appointment();
        ap.setPatient(patient);
        ap.setDoctor(doctor);
        ap.setStartTime(start);
        ap.setStatus("SCHEDULED");
        ap.setNotes(request.notes());

        Appointment saved = appointmentRepository.save(ap);
        log.info("Created appointment for patient={} with doctor={}", patient.getId(), doctor.getUsername());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsForDoctor(String doctorUsername) {
        return appointmentRepository.findByDoctor_Username(doctorUsername).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsForDoctorPaged(String doctorUsername, Pageable pageable) {
        return appointmentRepository.findByDoctor_Username(doctorUsername, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsForPatientUser(String patientUsername) {
        return appointmentRepository.findByPatient_User_Username(patientUsername).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsForPatientUserPaged(String patientUsername, Pageable pageable) {
        return appointmentRepository.findByPatient_User_Username(patientUsername, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointmentsPaged(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = findOrThrow(id);

        if (request.status() != null && !request.status().isBlank()) {
            appointment.setStatus(request.status());
        }
        if (request.notes() != null) {
            appointment.setNotes(request.notes());
        }
        if (request.startTime() != null && !request.startTime().isBlank()) {
            try {
                appointment.setStartTime(LocalDateTime.parse(request.startTime()));
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid startTime format, expected ISO-8601");
            }
        }

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Updated appointment id={}", id);
        return toResponse(saved);
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment = findOrThrow(id);
        appointmentRepository.delete(appointment);
        log.info("Deleted appointment id={}", id);
    }

    private void checkForConflicts(Long doctorId, LocalDateTime appointmentStart) {
        LocalDateTime windowStart = appointmentStart.minusMinutes(30);
        LocalDateTime windowEnd = appointmentStart.plusMinutes(90);

        List<Appointment> conflicts = appointmentRepository.findByDoctor_Id(doctorId).stream()
                .filter(a -> "SCHEDULED".equals(a.getStatus()))
                .filter(a -> !a.getStartTime().isAfter(windowEnd) && a.getStartTime().isBefore(windowStart.plusMinutes(30)))
                .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor has a conflict: appointment already scheduled in this time window");
        }
    }

    private Appointment findOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found: " + id));
    }

    public AppointmentResponse toResponse(Appointment a) {
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
