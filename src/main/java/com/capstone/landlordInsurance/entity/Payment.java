package com.capstone.landlordInsurance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private double amount;

    private String currency = "INR";

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    private String receiptId;

    private String paymentStatus = "pending";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime paymentDate;

    @ManyToOne
    @JoinColumn(name = "quote_id")
    private Quote quote;
}
