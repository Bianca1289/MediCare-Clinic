package com.medicareclinic.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column
    private String contactInfo;

    @Column
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 13)
    private String cnp;

    @Column(length = 10)
    private String gender;

    @Column(length = 255)
    private String address;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
