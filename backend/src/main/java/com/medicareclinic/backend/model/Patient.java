package com.medicareclinic.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Patient")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele este obligatoriu")
    @Size(min = 2, max = 50, message = "Numele trebuie să aibă între 2 și 50 de caractere")
    @Column(name = "nume", nullable = false, length = 50)
    private String nume;

    @NotBlank(message = "Prenumele este obligatoriu")
    @Size(min = 2, max = 50, message = "Prenumele trebuie să aibă între 2 și 50 de caractere")
    @Column(name = "prenume", nullable = false, length = 50)
    private String prenume;

    @NotBlank(message = "CNP-ul este obligatoriu")
    @Pattern(regexp = "\\d{13}", message = "CNP-ul trebuie să conțină exact 13 cifre")
    @Column(name = "cnp", nullable = false, unique = true, length = 13)
    private String cnp;

    @NotNull(message = "Data nașterii este obligatorie")
    @Past(message = "Data nașterii trebuie să fie în trecut")
    @Column(name = "data_nasterii", nullable = false)
    private LocalDate dataNasterii;

    @NotBlank(message = "Telefonul este obligatoriu")
    @Pattern(regexp = "^(\\+40|0)[0-9]{9}$", message = "Formatul telefonului este invalid")
    @Column(name = "telefon", nullable = false, length = 15)
    private String telefon;

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Formatul email-ului este invalid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Adresa este obligatorie")
    @Size(max = 200, message = "Adresa nu poate depăși 200 de caractere")
    @Column(name = "adresa", nullable = false, length = 200)
    private String adresa;

    @Size(max = 500, message = "Alergiile nu pot depăși 500 de caractere")
    @Column(name = "alergii", length = 500)
    private String alergii;

    @JsonIgnore
    @OneToMany(mappedBy = "pacient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consultation> consultatii = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "pacient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> retete = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "doctor_patient",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "doctor_id")
    )
    private List<Doctor> doctori = new ArrayList<>();
}
