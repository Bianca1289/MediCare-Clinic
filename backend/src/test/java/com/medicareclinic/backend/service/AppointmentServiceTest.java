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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private User doctor;
    private Appointment appointment;
    private final LocalDateTime futureTime = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);

    @BeforeEach
    void setUp() {
        doctor = new User();
        doctor.setId(10L);
        doctor.setUsername("doctor1");

        patient = new Patient();
        patient.setId(1L);
        patient.setFullName("Maria Ionescu");

        appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStartTime(futureTime);
        appointment.setStatus("SCHEDULED");
        appointment.setNotes("Initial visit");
    }

    @Test
    void createAppointment_success_returnsResponse() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                1L, "doctor1", futureTime.toString(), "Initial visit");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctor_Id(10L)).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.createAppointment(request);

        assertThat(response.patientId()).isEqualTo(1L);
        assertThat(response.doctorUsername()).isEqualTo("doctor1");
        assertThat(response.status()).isEqualTo("SCHEDULED");
    }

    @Test
    void createAppointment_patientNotFound_throwsBadRequest() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(99L, "doctor1", futureTime.toString(), null);
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patient not found");
    }

    @Test
    void createAppointment_doctorNotFound_throwsBadRequest() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(1L, "ghost", futureTime.toString(), null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Doctor user not found");
    }

    @Test
    void createAppointment_timeConflict_throwsConflict() {
        Appointment existing = new Appointment();
        existing.setDoctor(doctor);
        existing.setStartTime(futureTime.plusMinutes(15));
        existing.setStatus("SCHEDULED");

        CreateAppointmentRequest request = new CreateAppointmentRequest(1L, "doctor1", futureTime.toString(), null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctor_Id(10L)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("conflict");
    }

    @Test
    void createAppointment_invalidDateFormat_throwsBadRequest() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(1L, "doctor1", "not-a-date", null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid startTime");
    }

    @Test
    void getAppointmentsForDoctor_returnsCorrectList() {
        when(appointmentRepository.findByDoctor_Username("doctor1")).thenReturn(List.of(appointment));

        List<AppointmentResponse> result = appointmentService.getAppointmentsForDoctor("doctor1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).doctorUsername()).isEqualTo("doctor1");
    }

    @Test
    void getAppointmentsForDoctor_emptyList_returnsEmpty() {
        when(appointmentRepository.findByDoctor_Username("doctor1")).thenReturn(List.of());

        List<AppointmentResponse> result = appointmentService.getAppointmentsForDoctor("doctor1");

        assertThat(result).isEmpty();
    }

    @Test
    void updateAppointment_status_updatesSuccessfully() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest("COMPLETED", "Treated successfully", null);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.updateAppointment(5L, request);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.notes()).isEqualTo("Treated successfully");
    }

    @Test
    void updateAppointment_notFound_throwsNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateAppointmentRequest request = new UpdateAppointmentRequest("CANCELLED", null, null);

        assertThatThrownBy(() -> appointmentService.updateAppointment(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Appointment not found");
    }

    @Test
    void deleteAppointment_success_callsDelete() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));

        appointmentService.deleteAppointment(5L);

        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void deleteAppointment_notFound_throwsNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.deleteAppointment(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Appointment not found");
    }
}
