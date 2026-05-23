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

    @ManyToOne
    @JoinColumn(name = "quote_id")
    private Quote quote;

    private double amount;

    @Column(columnDefinition = "DEFAULT 'INR'")
    private String currency = "INR";

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    private String receiptId;

    @Column(columnDefinition = "DEFAULT 'pending'")
    private String paymentStatus = "pending";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime paymentDate;
}
