package com.capstone.landlordInsurance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resetPwdOtp")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPwdOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String brokerEmail;

    @NotNull
    private String otp;

    //By default it would be false
    private boolean otpUsedOnce;

    @NotNull
    @Column(updatable = false)
    private LocalDateTime expiryTime;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
