package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Premium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PremiumRepository extends JpaRepository<Premium, Long> {
    Optional<Premium> findByQuoteId(Long quoteId);
}
