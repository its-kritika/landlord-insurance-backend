package com.capstone.landlordInsurance.repository;

import com.capstone.landlordInsurance.entity.Premium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PremiumRepository extends JpaRepository<Premium, Long> {
    Optional<Premium> findByQuoteQuoteId(Long quoteId);

    @Modifying
    @Query("DELETE FROM Premium p WHERE p.quote.quoteId IN :quoteIds")
    void deleteAllByQuoteIds(@Param("quoteIds") List<Long> quoteIds);
}
