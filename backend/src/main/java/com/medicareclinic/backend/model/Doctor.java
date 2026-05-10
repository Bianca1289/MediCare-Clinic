package com.medicareclinic.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

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

    @NotBlank(message = "Specializarea este obligatorie")
    @Size(max = 100, message = "Specializarea nu poate depăși 100 de caractere")
    @Column(name = "specializare", nullable = false, length = 100)
    private String specializare;


    @NotBlank(message = "Telefonul este obligatoriu")
    @Pattern(regexp = "^(\\+40|0)[0-9]{9}$", message = "Formatul telefonului este invalid")
    @Column(name = "telefon", nullable = false, length = 15)
    private String telefon;

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Formatul email-ului este invalid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consultation> consultatii = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> retete = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorSchedule> programeLucru = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Patient> pacienti = new ArrayList<>();
}
