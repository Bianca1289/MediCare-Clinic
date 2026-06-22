package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.DoctorProfileRequest;
import com.medicareclinic.backend.dto.DoctorProfileResponse;
import com.medicareclinic.backend.dto.UpdateDoctorProfileRequest;
import com.medicareclinic.backend.model.DoctorProfile;
import com.medicareclinic.backend.model.Specialty;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.DoctorProfileRepository;
import com.medicareclinic.backend.repository.SpecialtyRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorProfileServiceTest {

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private DoctorProfileService doctorProfileService;

    private User doctorUser;
    private DoctorProfile profile;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        doctorUser = new User();
        doctorUser.setId(1L);
        doctorUser.setUsername("doctor1");

        specialty = new Specialty("Cardiology", "Heart");
        specialty.setId(10L);

        profile = new DoctorProfile();
        profile.setId(1L);
        profile.setUser(doctorUser);
        profile.setLicenseNumber("LIC-001");
        profile.setBio("Experienced cardiologist");
        profile.setPhoneNumber("+40700000001");
        profile.setSpecialties(Set.of(specialty));
    }

    @Test
    void create_success_returnsResponse() {
        DoctorProfileRequest request = new DoctorProfileRequest("doctor1", "LIC-001", "Bio", "+40700", Set.of(10L));

        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(doctorUser));
        when(doctorProfileRepository.existsByUser_Id(1L)).thenReturn(false);
        when(doctorProfileRepository.existsByLicenseNumber("LIC-001")).thenReturn(false);
        when(specialtyRepository.findById(10L)).thenReturn(Optional.of(specialty));
        when(doctorProfileRepository.save(any(DoctorProfile.class))).thenReturn(profile);

        DoctorProfileResponse response = doctorProfileService.create(request);

        assertThat(response.username()).isEqualTo("doctor1");
        assertThat(response.licenseNumber()).isEqualTo("LIC-001");
        assertThat(response.specialties()).contains("Cardiology");
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        DoctorProfileRequest request = new DoctorProfileRequest("ghost", "LIC-X", null, null, null);
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorProfileService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void create_duplicateProfile_throwsConflict() {
        DoctorProfileRequest request = new DoctorProfileRequest("doctor1", "LIC-001", null, null, null);
        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(doctorUser));
        when(doctorProfileRepository.existsByUser_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> doctorProfileService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_duplicateLicense_throwsConflict() {
        DoctorProfileRequest request = new DoctorProfileRequest("doctor1", "LIC-001", null, null, null);
        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(doctorUser));
        when(doctorProfileRepository.existsByUser_Id(1L)).thenReturn(false);
        when(doctorProfileRepository.existsByLicenseNumber("LIC-001")).thenReturn(true);

        assertThatThrownBy(() -> doctorProfileService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("License number already registered");
    }

    @Test
    void getAll_returnsPaginatedResults() {
        Page<DoctorProfile> page = new PageImpl<>(List.of(profile));
        PageRequest pageable = PageRequest.of(0, 10);

        when(doctorProfileRepository.findAll(pageable)).thenReturn(page);

        Page<DoctorProfileResponse> result = doctorProfileService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("doctor1");
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(doctorProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorProfileService.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Doctor profile not found");
    }

    @Test
    void update_success_updatesFields() {
        UpdateDoctorProfileRequest request = new UpdateDoctorProfileRequest("LIC-NEW", "New bio", "+40799", null);
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(doctorProfileRepository.existsByLicenseNumber("LIC-NEW")).thenReturn(false);
        when(doctorProfileRepository.save(any(DoctorProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        DoctorProfileResponse response = doctorProfileService.update(1L, request);

        assertThat(response.licenseNumber()).isEqualTo("LIC-NEW");
        assertThat(response.bio()).isEqualTo("New bio");
    }

    @Test
    void delete_success_callsDelete() {
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        doctorProfileService.delete(1L);

        verify(doctorProfileRepository).delete(profile);
    }
}
