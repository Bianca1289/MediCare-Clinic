package com.medicareclinic.backend.integration;

import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.Role;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.AppointmentRepository;
import com.medicareclinic.backend.repository.PatientRepository;
import com.medicareclinic.backend.repository.RoleRepository;
import com.medicareclinic.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Patient patient;
    private User doctor;
    private final String futureTime = LocalDateTime.now().plusDays(10).withSecond(0).withNano(0).toString();

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();

        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_DOCTOR")));
        Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_PATIENT")));

        if (!userRepository.existsByUsername("test_doctor")) {
            doctor = new User();
            doctor.setUsername("test_doctor");
            doctor.setPassword(passwordEncoder.encode("pass"));
            doctor.setEnabled(true);
            doctor.setRoles(new HashSet<>(Set.of(doctorRole)));
            doctor = userRepository.save(doctor);
        } else {
            doctor = userRepository.findByUsername("test_doctor").orElseThrow();
        }

        if (!userRepository.existsByUsername("test_patient_user")) {
            User patientUser = new User();
            patientUser.setUsername("test_patient_user");
            patientUser.setPassword(passwordEncoder.encode("pass"));
            patientUser.setEnabled(true);
            patientUser.setRoles(new HashSet<>(Set.of(patientRole)));
            patientUser = userRepository.save(patientUser);

            patient = new Patient();
            patient.setFullName("Test Patient");
            patient.setEmail("test@example.com");
            patient.setUser(patientUser);
            patient = patientRepository.save(patient);
        } else {
            patient = patientRepository.findByUser_Username("test_patient_user").orElseThrow();
        }
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void createAppointment_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/receptionist/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "patientId": %d,
                                  "doctorUsername": "test_doctor",
                                  "startTime": "%s",
                                  "notes": "First visit"
                                }
                                """, patient.getId(), futureTime)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.doctorUsername").value("test_doctor"))
                .andExpect(jsonPath("$.patientName").value("Test Patient"));
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void createAppointment_invalidPatient_returns400() throws Exception {
        mockMvc.perform(post("/api/receptionist/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "patientId": 9999,
                                  "doctorUsername": "test_doctor",
                                  "startTime": "%s"
                                }
                                """, futureTime)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test_doctor", roles = {"DOCTOR"})
    void doctorViewsOwnAppointments_returnsPaginatedList() throws Exception {
        // Create appointment first via receptionist
        appointmentRepository.deleteAll();

        mockMvc.perform(get("/api/doctor/appointments?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void updateAppointmentStatus_toCompleted_returns200() throws Exception {
        // Create appointment
        String createBody = String.format("""
                {
                  "patientId": %d,
                  "doctorUsername": "test_doctor",
                  "startTime": "%s",
                  "notes": "Check-up"
                }
                """, patient.getId(), futureTime);

        mockMvc.perform(post("/api/receptionist/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        Long appointmentId = appointmentRepository.findAll().get(0).getId();

        mockMvc.perform(put("/api/receptionist/appointments/" + appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED",
                                  "notes": "Appointment completed successfully"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void deleteAppointment_existingId_returns204() throws Exception {
        // Create appointment
        mockMvc.perform(post("/api/receptionist/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "patientId": %d,
                                  "doctorUsername": "test_doctor",
                                  "startTime": "%s"
                                }
                                """, patient.getId(), futureTime)))
                .andExpect(status().isCreated());

        Long appointmentId = appointmentRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/receptionist/appointments/" + appointmentId))
                .andExpect(status().isNoContent());
    }
}
