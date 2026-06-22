package com.medicareclinic.backend.integration;

import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.Role;
import com.medicareclinic.backend.model.User;
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

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
        // DataInitializer will re-populate via CommandLineRunner
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void createPatient_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/receptionist/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Andrei Muresan",
                                  "contactInfo": "+40733333333",
                                  "email": "andrei@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Andrei Muresan"))
                .andExpect(jsonPath("$.email").value("andrei@example.com"));
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void createPatient_missingFullName_returns400() throws Exception {
        mockMvc.perform(post("/api/receptionist/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "",
                                  "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void listPatients_returnsPaginatedResults() throws Exception {
        // Create a patient first
        mockMvc.perform(post("/api/receptionist/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "Elena Popa", "email": "elena@example.com"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/receptionist/patients?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void updatePatient_existingId_returns200() throws Exception {
        // Create patient
        String createResponse = mockMvc.perform(post("/api/receptionist/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "Original Name", "email": "orig@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract id from response (simple string search)
        Long id = patientRepository.findAll().stream()
                .filter(p -> "Original Name".equals(p.getFullName()))
                .findFirst()
                .map(Patient::getId)
                .orElseThrow();

        mockMvc.perform(put("/api/receptionist/patients/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Updated Name",
                                  "contactInfo": "+40799",
                                  "email": "updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"));
    }

    @Test
    @WithMockUser(username = "reception1", roles = {"RECEPTIONIST"})
    void deletePatient_existingId_returns204() throws Exception {
        // Create patient
        mockMvc.perform(post("/api/receptionist/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "To Be Deleted", "email": "del@example.com"}
                                """))
                .andExpect(status().isCreated());

        Long id = patientRepository.findAll().stream()
                .filter(p -> "To Be Deleted".equals(p.getFullName()))
                .findFirst()
                .map(Patient::getId)
                .orElseThrow();

        mockMvc.perform(delete("/api/receptionist/patients/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/receptionist/patients/" + id))
                .andExpect(status().isNotFound());
    }
}
