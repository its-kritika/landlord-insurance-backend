package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.LossOfIncomeCov;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LossOfIncomeRepository extends JpaRepository<LossOfIncomeCov, Long> {
}
