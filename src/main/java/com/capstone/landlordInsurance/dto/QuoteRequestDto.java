package com.capstone.landlordInsurance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteRequestDto {

    private Long clientId;  // Client ID selected in the form
    private Long brokerId;  // Broker ID (if needed)

    // Property Details
    private String propertyAddress;
    private String propertyZip;
    private String propertyType;
    private Double propertyValue;
    private Integer yearBuilt;
    private Double area;
    private Double deductibleValue;
    private Double coverageLimit;

    // Coverage Type (you can set this as a string or enum)
    private String coverageType;  // Example: "FIRE_WATER", "THEFT", "LOSS_OF_INCOME", "ALL_IN_ONE"

    // Fire and Water Coverage Details
    private String constructionType;
    private List<String> fireSafetySystem;
    private String proximityToFireStation;
    private String plumbingCondition;
    private String ageOfPlumbingSystem;
    private Boolean isFloodProneArea;

    // Vandalism and Theft Coverage Details
    private List<String> securityFeatures;
    private String neighborhoodCrimeRate;
    private Boolean previousIncidents;

    // Loss of Income Coverage Details
    private Double monthlyRentalIncome;
    private Integer numberOfTenants;
    private Boolean hasMortgage;
    private Integer vacantDaysLastYear;
    private Integer unitsRentedLastYear;
}
