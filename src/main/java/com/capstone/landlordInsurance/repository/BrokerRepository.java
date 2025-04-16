package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Broker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrokerRepository extends JpaRepository<Broker, Long> {
}
