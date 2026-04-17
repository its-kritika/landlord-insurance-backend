package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByClient_ClientId(Long clientId);

    List<Quote> findByBroker_BrokerId(Long brokerId);

    Page<Quote> findByBroker_BrokerIdAndStatusNot(Long brokerId, String status, Pageable pageable);

    long countByBroker_BrokerIdAndStatusNot(Long brokerBrokerId, String status);

    long countByBroker_BrokerIdAndStatus(Long brokerId, String bound);
}
