package com.capstone.landlordInsurance.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quotes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quoteId;

    private String propertyAddress;
    private String propertyZip;
    private String propertyType;
    private double propertyValue;
    private int yearBuilt;
    private double area;
    private double deductibleValue;
    private double coverageLimit;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'pending'")
    private String status = "pending";

//    private double calculatedPremium;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private String coverageType;

    @ManyToOne
    @JoinColumn(name = "broker_id", nullable = true)
    private Broker broker;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private FireWaterCov fireWaterCoverage;

    @OneToOne(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private VandalismTheftCov vandalismTheftCoverage;

    @OneToOne(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private LossOfIncomeCov lossOfIncomeCoverage;

    @OneToOne(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private Premium premium;

}

// create quote
//   {
//        "ageOfPlumbingSystem": "less5",
//        "area": 1200,
//        "brokerId": 1,
//        "clientId": 3,
//        "constructionType": "brick",
//        "coverageType": "All-In-One Coverage",
//        "fireSafetySystem": ["Smoke Detectors", "Fire Alarms"],
//        "hasMortgage": true,
//        "isFloodProneArea": false,
//        "monthlyRentalIncome": 120000,
//        "neighborhoodCrimeRate": "Medium",
//        "numberOfTenants": 2,
//        "plumbingCondition": "Fair",
//        "previousIncidents": true,
//        "propertyAddress": "10, Narayana Colony, Prayagraj, Uttar Pradesh",
//        "propertyType": "office",
//        "propertyValue": 2300000.5,
//        "propertyZip": "211006",
//        "proximityToFireStation": "bet48",
//        "securityFeatures": ["CCTV", "Gated Access", "Smart Lock"],
//        "unitsRentedLastYear": 20,
//        "vacantDaysLastYear": 120,
//        "yearBuilt": 2014,
//        "deductibleValue": 200000,
//        "coverageLimit" : 1500000
//     }


// update quote ( where coverage remains same)
//{
//        "ageOfPlumbingSystem": "less5",
//        "area": 1200,
//        "brokerId": 1,
//        "clientId": 3,
//        "constructionType": "brick",
//        "coverageType": "Fire & Water Damage Coverage",
//        "fireSafetySystem": ["Smoke Detectors"],
//        "isFloodProneArea": false,
//        "plumbingCondition": "Fair",
//        "propertyAddress": "10, Narayana Colony, Prayagraj, Uttar Pradesh",
//        "propertyType": "retail",
//        "propertyValue": 2300000.5,
//        "propertyZip": "211006",
//        "proximityToFireStation": "bet48",
//        "yearBuilt": 2014,
//        "deductibleValue": 200000,
//        "coverageLimit" : 1500000
//        }

//update quote (where coverage is changed All-In-One to Loss Of Income)
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
//        "vacantDaysLastYear": 120,
//        "yearBuilt": 2015
//        }