package com.capstone.landlordInsurance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"broker_id", "email"})} //A client should be unique for a broker
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clientId;
    private String name;

    @Column
    private String email;
    private String address;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be exactly 10 digits")
    @Column(length = 10)
    private String phone;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "broker_id", referencedColumnName = "brokerId", nullable = false)
    @JsonIgnore
    private Broker broker;
}

//   {
//        "name": "Sanjana",
//        "email": "san@gmail.com",
//        "address": "Prayagraj",
//        "phone": "9876553321",
//        "broker": {
//        "broker_id": 1
//        }
//    }
