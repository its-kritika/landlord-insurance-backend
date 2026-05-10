package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findTop5ByBroker_BrokerIdOrderByCreatedAtDesc(Long brokerId);
    List<Client> findByBroker_BrokerIdAndNameContainingIgnoreCase(Long brokerId, String query);

    List<Client> findByBroker_BrokerId(Long brokerId);

    @Query("""
        SELECT c FROM Client c
        WHERE c.broker.brokerId = :brokerId
        AND (
            LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.address) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    Page<Client> filterClients(
            @Param("brokerId") Long brokerId,
            @Param("search") String search,
            Pageable pageable
    );

    long countByBroker_BrokerId(Long brokerBrokerId);
}

