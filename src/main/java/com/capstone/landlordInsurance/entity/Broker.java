package com.capstone.landlordInsurance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brokers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Broker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brokerId;
    @NotNull
    private String name;

    @Column(unique = true)
    @NotNull
    private String email;

    @NotNull
    private String password;
}

//    {
//        "name" : "Kritika",
//        "email" : "Kriti@gmail.com",
//        "password": "kritikapass"
//    }
