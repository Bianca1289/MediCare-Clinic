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
@Table(name = "Receptionist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receptionist {

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

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Formatul email-ului este invalid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @OneToMany(mappedBy = "receptionist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consultation> consultatii = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "receptionist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Patient> pacienti = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "receptionist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorSchedule> programeLucru = new ArrayList<>();
}
