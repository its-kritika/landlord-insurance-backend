package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findTop5ByBroker_BrokerIdOrderByCreatedAtDesc(Long brokerId);
    List<Client> findByBroker_BrokerIdAndNameContainingIgnoreCase(Long brokerId, String query);

    List<Client> findByBroker_BrokerId(Long brokerId);
}

