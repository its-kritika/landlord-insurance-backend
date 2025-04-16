package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.FireWaterCov;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FireWaterRepository extends JpaRepository<FireWaterCov, Long> {
}

