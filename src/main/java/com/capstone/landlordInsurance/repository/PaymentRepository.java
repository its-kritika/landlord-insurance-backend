package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByQuoteQuoteIdAndPaymentStatus(Long quoteId, String paymentStatus);
}
