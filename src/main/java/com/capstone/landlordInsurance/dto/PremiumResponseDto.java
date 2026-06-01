package com.capstone.landlordInsurance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PremiumResponseDto {
    private Long quoteId;
    private String ClientName;
    private String clientEmail;
    private String coverageType;          // e.g., "FIRE_WATER", "THEFT", etc.
    private double basePremium;
    private double coverageLimit;
    private double deductible;
    private double propertyValue;
    private double calculatedPremium;
    private double discount;
    private double dynamicVal;
    private double tax;
    private LocalDateTime time;
    private LocalDateTime updatedAt;
    private String status;
}

//{
//        "area": 1200,
//        "brokerId": 1,
//        "clientId": 3,
//        "coverageType": "Vandalism & Theft Coverage",
//        "neighborhoodCrimeRate": "HIGH",
//        "previousIncidents": true,
//        "propertyAddress": "10, Narayana Colony, Prayagraj, Uttar Pradesh",
//        "propertyType": "industrial",
//        "propertyValue": 2300000.5,
//        "propertyZip": "211006",
//        "securityFeatures": ["CCTV", "Gated Access", "Smart Lock"],
//        "yearBuilt": 2007,
//        "deductibleValue": 120000,
//        "coverageLimit" : 1200000
//        }

//{
//        "area": 1200,
//        "brokerId": 1,
//        "clientId": 3,
//        "coverageType": "Loss of Rental Income Coverage",
//        "hasMortgage": true,
//        "monthlyRentalIncome": 120000,
//        "numberOfTenants": 2,
//        "propertyAddress": "10, Narayana Colony, Prayagraj, Uttar Pradesh",
//        "propertyType": "office",
//        "propertyValue": 2300000.5,
//        "propertyZip": "211006",
//        "unitsRentedLastYear": 20,
//        "vacantDaysLastYear": 300,
//        "yearBuilt": 2015,
//        "deductibleValue": 10000,
//        "coverageLimit" : 1000000
//        }


//  {
//          "ageOfPlumbingSystem": "more15",
//          "area": 1200,
//          "brokerId": 1,
//          "clientId": 3,
//          "constructionType": "brick",
//          "coverageType": "All-In-One Coverage",
//          "fireSafetySystem": ["Smoke Detectors", "Fire Alarms"],
//          "hasMortgage": true,
//          "isFloodProneArea": true,
//          "monthlyRentalIncome": 120000,
//          "neighborhoodCrimeRate": "Medium",
//          "numberOfTenants": 2,
//          "plumbingCondition": "poor",
//          "previousIncidents": true,
//          "propertyAddress": "10, Narayana Colony, Prayagraj, Uttar Pradesh",
//          "propertyType": "office",
//          "propertyValue": 230000.5,
//          "propertyZip": "211006",
//          "proximityToFireStation": "more8",
//          "securityFeatures": ["CCTV", "Gated Access", "Smart Lock"],
//          "unitsRentedLastYear": 20,
//          "vacantDaysLastYear": 120,
//          "yearBuilt": 2014,
//          "deductibleValue": 50000,
//          "coverageLimit" : 100000
//          }
