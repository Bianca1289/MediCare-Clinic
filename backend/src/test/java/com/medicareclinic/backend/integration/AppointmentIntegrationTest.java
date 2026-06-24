package com.medicareclinic.backend.integration;

import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.Role;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.AppointmentRepository;
import com.medicareclinic.backend.repository.PatientRepository;
import com.medicareclinic.backend.repository.PrescriptionRepository;
import com.medicareclinic.backend.repository.RoleRepository;
import com.medicareclinic.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AppointmentIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private Patient patient;
    private final String futureTime = LocalDateTime.now().plusDays(10).withSecond(0).withNano(0).toString();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();

        prescriptionRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();

        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_DOCTOR")));
        Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_PATIENT")));

        if (!userRepository.existsByUsername("test_doctor")) {
            User doctor = new User();
            doctor.setUsername("test_doctor");
            doctor.setPassword(passwordEncoder.encode("pass"));
            doctor.setEnabled(true);
            doctor.setRoles(new HashSet<>(Set.of(doctorRole)));
            userRepository.save(doctor);
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
                .andExpect(jsonPath("$.doctorUsername").value("test_doctor"));
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
        mockMvc.perform(get("/api/doctor/appointments?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void updateAppointmentStatus_toCompleted_returns200() throws Exception {
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

        mockMvc.perform(put("/api/receptionist/appointments/" + appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED",
                                  "notes": "Appointment completed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void deleteAppointment_existingId_returns204() throws Exception {
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
