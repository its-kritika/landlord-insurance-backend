package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
