package com.medicareclinic.backend.config;

import com.medicareclinic.backend.model.Role;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.RoleRepository;
import com.medicareclinic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createRolesIfAbsent();
        createDefaultUsersIfAbsent();
    }

    private void createRolesIfAbsent() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ROLE_PATIENT"));
            roleRepository.save(new Role("ROLE_DOCTOR"));
            roleRepository.save(new Role("ROLE_RECEPTIONIST"));
            roleRepository.save(new Role("ROLE_ADMIN"));
            log.info("Default roles created: ROLE_PATIENT, ROLE_DOCTOR, ROLE_RECEPTIONIST, ROLE_ADMIN");
        }
    }

    private void createDefaultUsersIfAbsent() {
        if (userRepository.count() == 0) {
            Role patientRole = roleRepository.findByName("ROLE_PATIENT").orElseThrow();
            Role doctorRole = roleRepository.findByName("ROLE_DOCTOR").orElseThrow();
            Role receptionistRole = roleRepository.findByName("ROLE_RECEPTIONIST").orElseThrow();
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            HashSet<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            admin.setRoles(adminRoles);
            userRepository.save(admin);

            User doctor = new User();
            doctor.setUsername("doctor1");
            doctor.setPassword(passwordEncoder.encode("doctor123"));
            doctor.setEnabled(true);
            HashSet<Role> doctorRoles = new HashSet<>();
            doctorRoles.add(doctorRole);
            userRepository.save(doctor);

            User receptionist = new User();
            receptionist.setUsername("reception1");
            receptionist.setPassword(passwordEncoder.encode("recept123"));
            receptionist.setEnabled(true);
            HashSet<Role> receptionRoles = new HashSet<>();
            receptionRoles.add(receptionistRole);
            userRepository.save(receptionist);

            User patient = new User();
            patient.setUsername("patient1");
            patient.setPassword(passwordEncoder.encode("patient123"));
            patient.setEnabled(true);
            HashSet<Role> patientRoles = new HashSet<>();
            patientRoles.add(patientRole);
            userRepository.save(patient);

            log.info("Default users created — admin:admin123, doctor:doctor123, reception:recept123, patient:patient123");
        }
    }
}
