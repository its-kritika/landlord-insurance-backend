package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByClient_ClientId(Long clientId);

    List<Quote> findByBroker_BrokerId(Long brokerId);

    Page<Quote> findByBroker_BrokerIdAndStatusNot(Long brokerId, String status, Pageable pageable);

    long countByBroker_BrokerIdAndStatusNot(Long brokerBrokerId, String status);

    long countByBroker_BrokerIdAndStatus(Long brokerId, String bound);

    Page<Quote> findByBroker_BrokerIdAndStatus(Long brokerId, String status, Pageable pageable);

    Page<Quote> findByBroker_BrokerIdAndStatusAndCreatedAtAfter(Long brokerBrokerId, String status, LocalDateTime createdAtAfter, Pageable pageable);

    Page<Quote> findByBroker_BrokerIdAndCreatedAtAfter(Long brokerBrokerId, LocalDateTime createdAtAfter, Pageable pageable);

//    Page<Quote> findByBroker_BrokerIdAndClientNameContainingIgnoreCaseOrCoverageTypeContainingIgnoreCase(
//            Long brokerId,
//            String clientName,
//            String coverageType,
//            Pageable pageable
//    );

    @Query("""
        SELECT q FROM Quote q
        WHERE q.broker.brokerId = :brokerId
        AND (
            LOWER(q.client.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(q.coverageType) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    Page<Quote> searchQuotes(
            @Param("brokerId") Long brokerId,
            @Param("search") String search,
            Pageable pageable
    );
}
