package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.VandalismTheftCov;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VandalismTheftRepository extends JpaRepository<VandalismTheftCov, Long> {
}
