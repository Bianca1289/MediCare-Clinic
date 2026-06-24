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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        cleanupLegacyDoctors();
        createRolesIfAbsent();
        createDefaultUsersIfAbsent();
        createSpecialtiesIfAbsent();
        createDoctorProfilesIfAbsent();
        createAvailabilityIfAbsent();
        createPatientsIfAbsent();
        createMedicalRecordsIfAbsent();
        createPastAppointmentsAndPrescriptions();
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private void cleanupLegacyDoctors() {
        removeLegacyDoctor("doctor1");
        removeLegacyDoctor("doctor2");
    }

    private void removeLegacyDoctor(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            // Remove prescriptions where this user is the doctor
            List<Prescription> prescriptions = prescriptionRepository.findAll().stream()
                    .filter(p -> p.getDoctor().getId().equals(user.getId()))
                    .toList();
            prescriptionRepository.deleteAll(prescriptions);

            // Remove appointments where this user is the doctor
            List<Appointment> appointments = appointmentRepository.findByDoctor_Id(user.getId());
            appointmentRepository.deleteAll(appointments);

            // Remove doctor profile (cascades to availability and specialty junction)
            doctorProfileRepository.findByUser_Username(username)
                    .ifPresent(doctorProfileRepository::delete);

            // Remove patient record if any (unlikely for doctors)
            patientRepository.findByUser_Username(username)
                    .ifPresent(patientRepository::delete);

            userRepository.delete(user);
            log.info("Removed legacy doctor user: {}", username);
        });
    }

    // ── Roles ─────────────────────────────────────────────────────────────────

    private void createRolesIfAbsent() {
        saveRoleIfAbsent("ROLE_PATIENT");
        saveRoleIfAbsent("ROLE_DOCTOR");
        saveRoleIfAbsent("ROLE_RECEPTIONIST");
        saveRoleIfAbsent("ROLE_ADMIN");
    }

    private void saveRoleIfAbsent(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(new Role(name));
        }
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    private void createDefaultUsersIfAbsent() {
        Role patientRole      = roleRepository.findByName("ROLE_PATIENT").orElseThrow();
        Role doctorRole       = roleRepository.findByName("ROLE_DOCTOR").orElseThrow();
        Role receptionistRole = roleRepository.findByName("ROLE_RECEPTIONIST").orElseThrow();
        Role adminRole        = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

        saveUserIfAbsent("admin",      "admin123",   adminRole);
        saveUserIfAbsent("reception1", "recept123",  receptionistRole);
        saveUserIfAbsent("patient1",   "patient123", patientRole);
        saveUserIfAbsent("patient2",   "patient123", patientRole);

        saveUserIfAbsent("dr.ionescu",    "doctor123", doctorRole);
        saveUserIfAbsent("dr.popescu",    "doctor123", doctorRole);
        saveUserIfAbsent("dr.andreescu",  "doctor123", doctorRole);
        saveUserIfAbsent("dr.stanescu",   "doctor123", doctorRole);
        saveUserIfAbsent("dr.constantin", "doctor123", doctorRole);
        saveUserIfAbsent("dr.mihai",      "doctor123", doctorRole);
        saveUserIfAbsent("dr.dumitru",    "doctor123", doctorRole);
        saveUserIfAbsent("dr.gheorghe",   "doctor123", doctorRole);
        saveUserIfAbsent("dr.popa",       "doctor123", doctorRole);
        saveUserIfAbsent("dr.moldovan",   "doctor123", doctorRole);
        saveUserIfAbsent("dr.radu",       "doctor123", doctorRole);

        log.info("Default users ensured");
    }

    private void saveUserIfAbsent(String username, String rawPassword, Role role) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(buildUser(username, rawPassword, role));
        }
    }

    // ── Specialties ───────────────────────────────────────────────────────────

    private void createSpecialtiesIfAbsent() {
        saveSpecialtyIfAbsent("Cardiology",      "Heart and cardiovascular system diseases");
        saveSpecialtyIfAbsent("General Medicine", "Primary care and preventive medicine");
        saveSpecialtyIfAbsent("Dermatology",      "Skin, hair and nail disorders");
        saveSpecialtyIfAbsent("Neurology",        "Brain and nervous system disorders");
        saveSpecialtyIfAbsent("Orthopedics",      "Musculoskeletal system, bones and joints");
        saveSpecialtyIfAbsent("Pediatrics",       "Medical care for infants, children and adolescents");
        saveSpecialtyIfAbsent("Ophthalmology",    "Eye diseases and vision disorders");
        saveSpecialtyIfAbsent("Psychiatry",       "Mental health and behavioral disorders");
        saveSpecialtyIfAbsent("Gynecology",       "Female reproductive system and health");
        saveSpecialtyIfAbsent("ENT",              "Ear, nose and throat disorders");
        saveSpecialtyIfAbsent("Urology",          "Urinary tract and male reproductive system");
        saveSpecialtyIfAbsent("Endocrinology",    "Hormonal and metabolic disorders");
    }

    private void saveSpecialtyIfAbsent(String name, String description) {
        if (!specialtyRepository.existsByName(name)) {
            specialtyRepository.save(new Specialty(name, description));
        }
    }

    // ── Doctor profiles ───────────────────────────────────────────────────────

    private void createDoctorProfilesIfAbsent() {
        Specialty cardiology    = spec("Cardiology");
        Specialty general       = spec("General Medicine");
        Specialty dermatology   = spec("Dermatology");
        Specialty neurology     = spec("Neurology");
        Specialty orthopedics   = spec("Orthopedics");
        Specialty pediatrics    = spec("Pediatrics");
        Specialty ophthalmology = spec("Ophthalmology");
        Specialty psychiatry    = spec("Psychiatry");
        Specialty gynecology    = spec("Gynecology");
        Specialty urology       = spec("Urology");

        saveDoctor("dr.ionescu",
                "LIC-101", "+40700000101", "Bucharest", 4.8,
                "Board-certified cardiologist with 12 years of experience in interventional cardiology and heart failure management. Trained at Spitalul Clinic de Urgență București.",
                cardiology);

        saveDoctor("dr.popescu",
                "LIC-102", "+40700000102", "Cluj-Napoca", 4.9,
                "Specialist in pediatrics with extensive experience caring for newborns, children and adolescents. Active researcher in pediatric immunology.",
                pediatrics);

        saveDoctor("dr.andreescu",
                "LIC-103", "+40700000103", "Timișoara", 4.7,
                "Orthopedic surgeon specializing in minimally invasive joint replacement and sports medicine. Over 1,000 successful knee and hip surgeries.",
                orthopedics);

        saveDoctor("dr.stanescu",
                "LIC-104", "+40700000104", "Cluj-Napoca", 4.4,
                "Neurologist with particular expertise in headache disorders, vertigo and chronic sinusitis. Bilingual consultations available.",
                neurology);

        saveDoctor("dr.constantin",
                "LIC-105", "+40700000105", "Iași", 4.3,
                "Ophthalmologist with 8 years of clinical practice. Skilled in cataract surgery, refractive procedures and treatment of macular degeneration.",
                ophthalmology);

        saveDoctor("dr.mihai",
                "LIC-106", "+40700000106", "Oradea", 4.8,
                "Gynecologist specializing in hormonal disorders, fertility issues and women's preventive health. Over 15 years of practice.",
                gynecology);

        saveDoctor("dr.dumitru",
                "LIC-107", "+40700000107", "Constanța", 4.6,
                "Psychiatrist with a patient-centered approach to depression, anxiety and personality disorders. Certified in cognitive-behavioral therapy (CBT).",
                psychiatry);

        saveDoctor("dr.gheorghe",
                "LIC-108", "+40700000108", "Brașov", 4.5,
                "Dermatologist offering integrated care for skin conditions with a special interest in onco-dermatology and laser therapy.",
                dermatology);

        saveDoctor("dr.popa",
                "LIC-109", "+40700000109", "Sibiu", 4.5,
                "Cardiologist with 9 years of experience in non-invasive cardiology, echocardiography and preventive cardiology. Specialist in arrhythmia management.",
                cardiology);

        saveDoctor("dr.moldovan",
                "LIC-110", "+40700000110", "Pitești", 4.3,
                "Dermatologist specializing in cosmetic dermatology, acne treatment and skin cancer screening. Trained in laser therapy and PRP procedures.",
                dermatology);

        saveDoctor("dr.radu",
                "LIC-111", "+40700000111", "Bucharest", 4.7,
                "General practitioner with broad expertise in family medicine, chronic disease management and preventive healthcare. Trusted by over 500 regular patients.",
                general);

        saveDoctor("dr.gheorghe.uro",
                "LIC-112", "+40700000112", "Brașov", 4.2,
                "Urologist with extensive experience in urinary tract disorders and minimally invasive urological procedures.",
                urology);

        log.info("Doctor profiles ensured");
    }

    private void saveDoctor(String username, String licenseNumber, String phone,
                            String location, double rating, String bio,
                            Specialty specialty) {
        userRepository.findByUsername(username).ifPresent(user -> {
            DoctorProfile profile = doctorProfileRepository.findByUser_Username(username)
                    .orElse(null);
            if (profile == null) {
                if (doctorProfileRepository.existsByLicenseNumber(licenseNumber)) return;
                profile = new DoctorProfile();
                profile.setUser(user);
                profile.setLicenseNumber(licenseNumber);
                profile.setPhoneNumber(phone);
                profile.setLocation(location);
                profile.setAverageRating(rating);
                profile.setBio(bio);
            }
            profile.setSpecialty(specialty);
            doctorProfileRepository.save(profile);
        });
    }

    private Specialty spec(String name) {
        return specialtyRepository.findByName(name).orElseThrow(
                () -> new IllegalStateException("Specialty not found: " + name));
    }

    // ── Availability ──────────────────────────────────────────────────────────

    private void createAvailabilityIfAbsent() {
        slot("dr.ionescu", "MONDAY",    "09:00", "13:00");
        slot("dr.ionescu", "TUESDAY",   "09:00", "13:00");
        slot("dr.ionescu", "WEDNESDAY", "14:00", "18:00");
        slot("dr.ionescu", "THURSDAY",  "09:00", "13:00");
        slot("dr.ionescu", "FRIDAY",    "09:00", "13:00");

        slot("dr.popescu", "MONDAY",    "08:00", "16:00");
        slot("dr.popescu", "TUESDAY",   "08:00", "16:00");
        slot("dr.popescu", "WEDNESDAY", "08:00", "16:00");
        slot("dr.popescu", "THURSDAY",  "08:00", "16:00");
        slot("dr.popescu", "FRIDAY",    "08:00", "16:00");
        slot("dr.popescu", "SATURDAY",  "09:00", "12:00");

        slot("dr.andreescu", "MONDAY",    "08:00", "14:00");
        slot("dr.andreescu", "WEDNESDAY", "08:00", "14:00");
        slot("dr.andreescu", "FRIDAY",    "08:00", "14:00");

        slot("dr.stanescu", "TUESDAY",   "10:00", "16:00");
        slot("dr.stanescu", "WEDNESDAY", "10:00", "16:00");
        slot("dr.stanescu", "THURSDAY",  "10:00", "16:00");
        slot("dr.stanescu", "SATURDAY",  "09:00", "12:00");

        slot("dr.constantin", "MONDAY",   "08:00", "12:00");
        slot("dr.constantin", "TUESDAY",  "13:00", "17:00");
        slot("dr.constantin", "THURSDAY", "08:00", "12:00");
        slot("dr.constantin", "FRIDAY",   "13:00", "17:00");

        slot("dr.mihai", "MONDAY",    "09:00", "14:00");
        slot("dr.mihai", "TUESDAY",   "09:00", "14:00");
        slot("dr.mihai", "WEDNESDAY", "09:00", "14:00");
        slot("dr.mihai", "THURSDAY",  "09:00", "14:00");
        slot("dr.mihai", "FRIDAY",    "09:00", "14:00");
        slot("dr.mihai", "SATURDAY",  "09:00", "12:00");

        slot("dr.dumitru", "MONDAY",    "10:00", "14:00");
        slot("dr.dumitru", "WEDNESDAY", "10:00", "14:00");
        slot("dr.dumitru", "THURSDAY",  "14:00", "18:00");
        slot("dr.dumitru", "FRIDAY",    "10:00", "14:00");

        slot("dr.gheorghe", "TUESDAY",  "09:00", "13:00");
        slot("dr.gheorghe", "THURSDAY", "09:00", "13:00");
        slot("dr.gheorghe", "SATURDAY", "09:00", "13:00");

        slot("dr.popa", "TUESDAY",   "09:00", "13:00");
        slot("dr.popa", "WEDNESDAY", "09:00", "13:00");
        slot("dr.popa", "THURSDAY",  "09:00", "13:00");
        slot("dr.popa", "FRIDAY",    "14:00", "18:00");

        slot("dr.moldovan", "MONDAY",    "10:00", "16:00");
        slot("dr.moldovan", "WEDNESDAY", "10:00", "16:00");
        slot("dr.moldovan", "SATURDAY",  "09:00", "13:00");

        slot("dr.radu", "MONDAY",    "08:00", "12:00");
        slot("dr.radu", "TUESDAY",   "08:00", "12:00");
        slot("dr.radu", "WEDNESDAY", "08:00", "12:00");
        slot("dr.radu", "THURSDAY",  "08:00", "12:00");
        slot("dr.radu", "FRIDAY",    "08:00", "12:00");

        log.info("Doctor availability ensured");
    }

    private void slot(String username, String day, String start, String end) {
        doctorProfileRepository.findByUser_Username(username).ifPresent(profile -> {
            if (!doctorAvailabilityRepository.existsByDoctorProfile_IdAndDayOfWeek(profile.getId(), day)) {
                DoctorAvailability av = new DoctorAvailability();
                av.setDoctorProfile(profile);
                av.setDayOfWeek(day);
                av.setStartTime(LocalTime.parse(start));
                av.setEndTime(LocalTime.parse(end));
                doctorAvailabilityRepository.save(av);
            }
        });
    }

    // ── Patients ──────────────────────────────────────────────────────────────

    private void createPatientsIfAbsent() {
        savePatient("patient1", "Maria Ionescu", "+40711111111",
                "maria.ionescu@example.com", "Female",
                "Strada Florilor 12, Sector 1, București");

        savePatient("patient2", "Ion Popescu", "+40722222222",
                "ion.popescu@example.com", "Male",
                "Strada Victoriei 5, Cluj-Napoca");
    }

    private void savePatient(String username, String fullName, String phone,
                             String email, String gender, String address) {
        userRepository.findByUsername(username).ifPresent(u -> {
            if (patientRepository.findByUser_Username(username).isEmpty()) {
                Patient p = new Patient();
                p.setUser(u);
                p.setFullName(fullName);
                p.setPhone(phone);
                p.setEmail(email);
                p.setGender(gender);
                p.setAddress(address);
                patientRepository.save(p);
            }
        });
    }

    // ── Medical records ───────────────────────────────────────────────────────

    private void createMedicalRecordsIfAbsent() {
        patientRepository.findByUser_Username("patient1").ifPresent(p1 -> {
            if (!medicalRecordRepository.existsByPatient_Id(p1.getId())) {
                MedicalRecord r = new MedicalRecord();
                r.setPatient(p1);
                r.setBloodType("A+");
                r.setAllergies("Penicillin");
                r.setChronicConditions("Hypertension, Hypercholesterolemia");
                r.setNotes("Regular check-ups required. Blood pressure monitoring monthly.");
                r.setDateOfBirth(LocalDate.of(1985, 3, 15));
                medicalRecordRepository.save(r);
            }
        });

        patientRepository.findByUser_Username("patient2").ifPresent(p2 -> {
            if (!medicalRecordRepository.existsByPatient_Id(p2.getId())) {
                MedicalRecord r = new MedicalRecord();
                r.setPatient(p2);
                r.setBloodType("O-");
                r.setAllergies("None known");
                r.setChronicConditions("Chronic migraines");
                r.setNotes("Prone to vertigo episodes. Avoid excessive screen time.");
                r.setDateOfBirth(LocalDate.of(1992, 7, 22));
                medicalRecordRepository.save(r);
            }
        });

        log.info("Medical records ensured");
    }

    // ── Past appointments & prescriptions ────────────────────────────────────

    private void createPastAppointmentsAndPrescriptions() {
        // patient1 – Maria Ionescu
        Appointment appt1 = saveCompletedAppointment("patient1", "dr.ionescu",
                LocalDateTime.of(2026, 5, 10, 9, 0),
                "Mild chest pain and fatigue. Stress-induced angina suspected.");

        Appointment appt2 = saveCompletedAppointment("patient1", "dr.mihai",
                LocalDateTime.of(2026, 4, 15, 10, 0),
                "Annual gynecological check-up. Hormonal panel requested.");

        Appointment appt3 = saveCompletedAppointment("patient1", "dr.andreescu",
                LocalDateTime.of(2026, 5, 28, 8, 0),
                "Right knee pain after jogging. Mild chondromalacia patellae.");

        // patient2 – Ion Popescu
        Appointment appt4 = saveCompletedAppointment("patient2", "dr.stanescu",
                LocalDateTime.of(2026, 5, 5, 11, 0),
                "Recurring headaches and vertigo episodes for 3 months.");

        Appointment appt5 = saveCompletedAppointment("patient2", "dr.radu",
                LocalDateTime.of(2026, 6, 2, 9, 0),
                "General check-up. Blood sugar slightly elevated.");

        // Prescriptions
        savePrescription(appt1,
                "Atorvastatin", "20 mg", "Once daily (evening)", "90 days",
                "Take in the evening, preferably at the same time each day. Avoid grapefruit juice. Report any muscle pain or weakness immediately. Schedule a lipid panel in 6 weeks.",
                LocalDate.of(2026, 8, 8));

        savePrescription(appt2,
                "Progynova (Estradiol valerate)", "2 mg", "Once daily", "28 days",
                "Take at the same time each day. Report any unusual bleeding, breast tenderness or mood changes. Follow-up appointment in 3 months.",
                LocalDate.of(2026, 7, 13));

        savePrescription(appt3,
                "Diclofenac Sodium", "75 mg", "Twice daily (with meals)", "14 days",
                "Take with food to reduce gastric irritation. Apply ice pack to knee for 15 minutes 3 times daily. Avoid other NSAIDs. Physiotherapy sessions recommended. Rest from running for at least 4 weeks.",
                LocalDate.of(2026, 6, 11));

        savePrescription(appt4,
                "Carbamazepine", "200 mg", "Twice daily", "30 days",
                "Take with food. Avoid alcohol entirely. Do not drive until stabilised. Monitor CBC and liver function monthly. Report any skin rash, fever or unusual bruising immediately.",
                LocalDate.of(2026, 7, 5));

        savePrescription(appt5,
                "Metformin", "500 mg", "Twice daily (with breakfast and dinner)", "60 days",
                "Take with meals to reduce GI side effects. Follow low-sugar diet. Monitor fasting blood glucose weekly. Return for HbA1c in 2 months.",
                LocalDate.of(2026, 8, 2));

        log.info("Past appointments and prescriptions ensured");
    }

    private Appointment saveCompletedAppointment(String patientUsername, String doctorUsername,
                                                  LocalDateTime startTime, String notes) {
        Optional<Patient> patientOpt = patientRepository.findByUser_Username(patientUsername);
        Optional<User> doctorOpt = userRepository.findByUsername(doctorUsername);
        if (patientOpt.isEmpty() || doctorOpt.isEmpty()) return null;

        Patient patient = patientOpt.get();
        User doctor = doctorOpt.get();

        return appointmentRepository.findByPatient_Id(patient.getId()).stream()
                .filter(a -> a.getDoctor().getUsername().equals(doctorUsername)
                          && a.getStartTime().equals(startTime))
                .findFirst()
                .orElseGet(() -> {
                    Appointment a = new Appointment();
                    a.setPatient(patient);
                    a.setDoctor(doctor);
                    a.setStartTime(startTime);
                    a.setStatus("COMPLETED");
                    a.setNotes(notes);
                    return appointmentRepository.save(a);
                });
    }

    private void savePrescription(Appointment appointment,
                                   String medication, String dosage,
                                   String frequency, String duration,
                                   String instructions, LocalDate validUntil) {
        if (appointment == null) return;
        if (prescriptionRepository.existsByAppointment_Id(appointment.getId())) return;

        Prescription p = new Prescription();
        p.setPatient(appointment.getPatient());
        p.setDoctor(appointment.getDoctor());
        p.setAppointment(appointment);
        p.setMedicationName(medication);
        p.setDosage(dosage);
        p.setFrequency(frequency);
        p.setDuration(duration);
        p.setInstructions(instructions);
        p.setIssuedAt(appointment.getStartTime());
        p.setValidUntil(validUntil);
        prescriptionRepository.save(p);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
