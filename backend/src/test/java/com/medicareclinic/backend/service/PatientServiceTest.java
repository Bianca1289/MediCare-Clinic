package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.CreatePatientRequest;
import com.medicareclinic.backend.dto.UpdatePatientRequest;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.PatientRepository;
import com.medicareclinic.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("patient1");

        patient = new Patient();
        patient.setId(1L);
        patient.setFullName("Maria Ionescu");
        patient.setEmail("maria@example.com");
        patient.setContactInfo("+40711111111");
        patient.setUser(user);
    }

    @Test
    void createPatient_withoutUser_savesSuccessfully() {
        CreatePatientRequest request = new CreatePatientRequest("Ion Pop", "+40700", "ion@ex.com", null);
        Patient saved = new Patient();
        saved.setId(2L);
        saved.setFullName("Ion Pop");

        when(patientRepository.save(any(Patient.class))).thenReturn(saved);

        Patient result = patientService.createPatient(request);

        assertThat(result.getFullName()).isEqualTo("Ion Pop");
        verify(patientRepository).save(any(Patient.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    void createPatient_withValidUser_linksUserToPatient() {
        CreatePatientRequest request = new CreatePatientRequest("Maria Ionescu", "+40711", "maria@ex.com", "patient1");

        when(userRepository.findByUsername("patient1")).thenReturn(Optional.of(user));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient result = patientService.createPatient(request);

        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo("patient1");
    }

    @Test
    void createPatient_withInvalidUser_throwsBadRequest() {
        CreatePatientRequest request = new CreatePatientRequest("Test", "", "", "unknown");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.createPatient(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getById_existingId_returnsPatient() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Patient result = patientService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Maria Ionescu");
    }

    @Test
    void getById_nonExistentId_throwsNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patient not found");
    }

    @Test
    void getPatientForUser_existingUsername_returnsPatient() {
        when(patientRepository.findByUser_Username("patient1")).thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientForUser("patient1");

        assertThat(result.getUser().getUsername()).isEqualTo("patient1");
    }

    @Test
    void getPatientForUser_notFound_throwsNotFound() {
        when(patientRepository.findByUser_Username("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientForUser("ghost"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patient record not found");
    }

    @Test
    void getAllPatientsPaged_returnsPage() {
        Page<Patient> page = new PageImpl<>(List.of(patient));
        PageRequest pageable = PageRequest.of(0, 10);

        when(patientRepository.findAll(pageable)).thenReturn(page);

        Page<?> result = patientService.getAllPatientsPaged(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updatePatient_success_updatesFields() {
        UpdatePatientRequest request = new UpdatePatientRequest("Novo Nome", "+40799", "novo@ex.com");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = patientService.updatePatient(1L, request);

        assertThat(response.fullName()).isEqualTo("Novo Nome");
        assertThat(response.email()).isEqualTo("novo@ex.com");
    }

    @Test
    void updatePatient_notFound_throwsNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        UpdatePatientRequest request = new UpdatePatientRequest("X", "", "");

        assertThatThrownBy(() -> patientService.updatePatient(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patient not found");
    }

    @Test
    void deletePatient_success_callsDelete() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        patientService.deletePatient(1L);

        verify(patientRepository).delete(patient);
    }

    @Test
    void deletePatient_notFound_throwsNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.deletePatient(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patient not found");
    }
}
