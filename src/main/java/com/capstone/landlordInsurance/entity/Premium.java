package com.capstone.landlordInsurance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Premium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long premiumId;

    private Long quoteId;
    private String clientName;
    private String clientEmail;
    private String coverageType;          // e.g., "FIRE_WATER", "THEFT", etc.
    private double basePremium;
    private double coverageLimit;
    private double deductible;
    private double propertyValue;
    private double calculatedPremium;
    private double discount;
    private double tax;
    private LocalDateTime time;
}
