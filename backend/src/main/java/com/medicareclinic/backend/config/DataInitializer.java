package com.medicareclinic.backend.config;

import com.medicareclinic.backend.model.*;
import com.medicareclinic.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Override
    @Transactional
    public void run(String... args) {
        createRolesIfAbsent();
        createDefaultUsersIfAbsent();
        createSpecialtiesIfAbsent();
        createDoctorProfilesIfAbsent();
        createPatientsIfAbsent();
        createMedicalRecordsIfAbsent();
    }

    private void createRolesIfAbsent() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ROLE_PATIENT"));
            roleRepository.save(new Role("ROLE_DOCTOR"));
            roleRepository.save(new Role("ROLE_RECEPTIONIST"));
            roleRepository.save(new Role("ROLE_ADMIN"));
            log.info("Default roles created");
        }
    }

    private void createDefaultUsersIfAbsent() {
        if (userRepository.count() == 0) {
            Role patientRole = roleRepository.findByName("ROLE_PATIENT").orElseThrow();
            Role doctorRole = roleRepository.findByName("ROLE_DOCTOR").orElseThrow();
            Role receptionistRole = roleRepository.findByName("ROLE_RECEPTIONIST").orElseThrow();
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

            userRepository.save(buildUser("admin", "admin123", adminRole));
            userRepository.save(buildUser("doctor1", "doctor123", doctorRole));
            userRepository.save(buildUser("doctor2", "doctor123", doctorRole));
            userRepository.save(buildUser("reception1", "recept123", receptionistRole));
            userRepository.save(buildUser("patient1", "patient123", patientRole));
            userRepository.save(buildUser("patient2", "patient123", patientRole));

            log.info("Default users created");
        }
    }

    private void createSpecialtiesIfAbsent() {
        if (specialtyRepository.count() == 0) {
            specialtyRepository.save(new Specialty("Cardiology", "Heart and cardiovascular system"));
            specialtyRepository.save(new Specialty("General Medicine", "Primary care and preventive medicine"));
            specialtyRepository.save(new Specialty("Dermatology", "Skin, hair and nail disorders"));
            specialtyRepository.save(new Specialty("Neurology", "Brain and nervous system disorders"));
            log.info("Default specialties created");
        }
    }

    private void createDoctorProfilesIfAbsent() {
        if (doctorProfileRepository.count() == 0) {
            Specialty cardiology = specialtyRepository.findByName("Cardiology").orElseThrow();
            Specialty general = specialtyRepository.findByName("General Medicine").orElseThrow();

            User doctor1 = userRepository.findByUsername("doctor1").orElseThrow();
            DoctorProfile profile1 = new DoctorProfile();
            profile1.setUser(doctor1);
            profile1.setLicenseNumber("LIC-001");
            profile1.setBio("Specialist in cardiology with 10 years of experience.");
            profile1.setPhoneNumber("+40700000001");
            profile1.setSpecialties(Set.of(cardiology, general));
            doctorProfileRepository.save(profile1);

            User doctor2 = userRepository.findByUsername("doctor2").orElseThrow();
            DoctorProfile profile2 = new DoctorProfile();
            profile2.setUser(doctor2);
            profile2.setLicenseNumber("LIC-002");
            profile2.setBio("General practitioner with focus on preventive medicine.");
            profile2.setPhoneNumber("+40700000002");
            profile2.setSpecialties(Set.of(general));
            doctorProfileRepository.save(profile2);

            log.info("Default doctor profiles created");
        }
    }

    private void createPatientsIfAbsent() {
        if (patientRepository.count() == 0) {
            User u1 = userRepository.findByUsername("patient1").orElseThrow();
            Patient p1 = new Patient();
            p1.setFullName("Maria Ionescu");
            p1.setContactInfo("+40711111111");
            p1.setEmail("maria.ionescu@example.com");
            p1.setUser(u1);
            patientRepository.save(p1);

            User u2 = userRepository.findByUsername("patient2").orElseThrow();
            Patient p2 = new Patient();
            p2.setFullName("Ion Popescu");
            p2.setContactInfo("+40722222222");
            p2.setEmail("ion.popescu@example.com");
            p2.setUser(u2);
            patientRepository.save(p2);

            log.info("Default patients created");
        }
    }

    private void createMedicalRecordsIfAbsent() {
        if (medicalRecordRepository.count() == 0) {
            Patient p1 = patientRepository.findByUser_Username("patient1").orElseThrow();
            MedicalRecord r1 = new MedicalRecord();
            r1.setPatient(p1);
            r1.setBloodType("A+");
            r1.setAllergies("Penicillin");
            r1.setChronicConditions("Hypertension");
            r1.setNotes("Regular check-ups required.");
            r1.setDateOfBirth(LocalDate.of(1985, 3, 15));
            medicalRecordRepository.save(r1);

            Patient p2 = patientRepository.findByUser_Username("patient2").orElseThrow();
            MedicalRecord r2 = new MedicalRecord();
            r2.setPatient(p2);
            r2.setBloodType("O-");
            r2.setAllergies("None");
            r2.setChronicConditions("None");
            r2.setNotes("Healthy patient, annual physical only.");
            r2.setDateOfBirth(LocalDate.of(1992, 7, 22));
            medicalRecordRepository.save(r2);

            log.info("Default medical records created");
        }
    }

    private User buildUser(String username, String rawPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        return user;
    }
}
