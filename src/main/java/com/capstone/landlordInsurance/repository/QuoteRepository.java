package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByClient_ClientId(Long clientId);

    List<Quote> findByBroker_BrokerId(Long brokerId);
}
