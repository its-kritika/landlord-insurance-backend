package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.dto.PremiumResponseDto;
import com.capstone.landlordInsurance.dto.QuoteRequestDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static com.capstone.landlordInsurance.utils.QuoteUtils.calculateVacancyRate;

@Service
public class CalculatePremiumService {

    public PremiumResponseDto getPremium(QuoteRequestDto request) {

        double basePremium =  (request.getCoverageLimit()* 0.005) + (request.getPropertyValue() * 0.002) - (request.getDeductibleValue() * 0.01);
//        System.out.println(basePremium);

        PremiumResponseDto response = new PremiumResponseDto();
        double modifier = calculateCommonFactor(request);

        double taxes = 0.0 ;
        double totalPremium = 0.0;
        double adjustedBasePremium = 0.0;
        String coverageType = request.getCoverageType();

        switch (coverageType) {
            case "Fire & Water Damage Coverage" -> {
                double firePremiumModifier = calculateFirePremium(request);
                adjustedBasePremium = basePremium * (modifier + firePremiumModifier);  // base * modifier

                response.setDiscount(0.0);
                BigDecimal roundedBasePremium = BigDecimal.valueOf(basePremium).setScale(2, RoundingMode.HALF_UP);
                response.setBasePremium(roundedBasePremium.doubleValue());
            }
            case "Vandalism & Theft Coverage" -> {
                double theftPremiumModifier = calculateTheftPremium(request);
                adjustedBasePremium = basePremium * (modifier + theftPremiumModifier);

                response.setDiscount(0.0);
                BigDecimal roundedBasePremium = BigDecimal.valueOf(basePremium).setScale(2, RoundingMode.HALF_UP);
                response.setBasePremium(roundedBasePremium.doubleValue());
            }
            case "Loss of Rental Income Coverage" -> {
                List<Double> lossPremium = calculateLossPremium(request);
                adjustedBasePremium = lossPremium.getFirst() * (modifier + lossPremium.getLast());

                response.setDiscount(0.0);
                BigDecimal roundedBasePremium = BigDecimal.valueOf(lossPremium.getFirst()).setScale(2, RoundingMode.HALF_UP);
                response.setBasePremium(roundedBasePremium.doubleValue());

            }
            case null, default -> {
                double firePremiumModifiers = calculateFirePremium(request);
                double theftPremiumModifiers = calculateTheftPremium(request);
                List<Double> lossPremium = calculateLossPremium(request);

                double allBasePremium = (basePremium + basePremium + lossPremium.getFirst()) * 0.7;
                double allModifiers = modifier + firePremiumModifiers + theftPremiumModifiers + lossPremium.getLast();
                adjustedBasePremium = allModifiers * allBasePremium * 0.85; //bundle discount

                BigDecimal roundedBasePremium = BigDecimal.valueOf(allBasePremium).setScale(2, RoundingMode.HALF_UP);
                response.setBasePremium(roundedBasePremium.doubleValue());
                response.setDiscount(allModifiers * allBasePremium * 0.15);
                System.out.println("modifiers" + allModifiers);
            }
        }

        taxes = adjustedBasePremium * 0.12;
        totalPremium = adjustedBasePremium + taxes;

        response.setCoverageType(request.getCoverageType());
        response.setDeductible(request.getDeductibleValue());
        response.setCoverageLimit(request.getCoverageLimit());
        response.setPropertyValue(request.getPropertyValue());
        response.setTax(taxes);

        BigDecimal roundedPremium = BigDecimal.valueOf(totalPremium).setScale(2, RoundingMode.HALF_UP);
        response.setCalculatedPremium(roundedPremium.doubleValue());

        return response;
    }

    private double calculateFirePremium(QuoteRequestDto request) {
        return getFireModifiers(request);
    }

    private double calculateTheftPremium(QuoteRequestDto request) {
        return getTheftModifiers(request);
    }

    private List<Double> calculateLossPremium(QuoteRequestDto request) {
        double basePremium = (request.getCoverageLimit()* 0.005) + (request.getMonthlyRentalIncome() * 0.003) - (request.getDeductibleValue() * 0.01);
//        System.out.println("base"+ basePremium);
        double modifiers = getLossModifiers(request);
        return List.of(basePremium, modifiers);
    }

    private double calculateCommonFactor(QuoteRequestDto request) {
        double modifier = 0.02;
        if (request.getYearBuilt() >= 2010) {
            modifier += 0.3;
        } else {
            modifier += 0.83;
        }
        double propertyModifier = switch (request.getPropertyType().toUpperCase()) {
            case "RETAIL" -> 0.12;
            case "OFFICE" -> 0.04;
            case "WAREHOUSE" -> 0.09;
            case "INDUSTRIAL" -> 0.18;
            default -> 0.19;
        };
        modifier += propertyModifier;
        return modifier;
    }

    private double getFireModifiers(QuoteRequestDto request) {

        double modifier = 0.1; // Base modifier

        // 1. Construction Type
        switch (request.getConstructionType().toUpperCase()) {
            case "WOOD": modifier += 0.28; break; // Higher risk
            case "BRICK": modifier += 0.17; break;
            case "STEEL": modifier += 0.07; break;
            case "CONCRETE": modifier += 0.11; break;
            default: modifier += 0.02;
        }

        // 2. Fire Safety Systems (additive bonuses)
        if (request.getFireSafetySystem() != null) {
            for (String system : request.getFireSafetySystem()) {
                switch (system.toUpperCase()) {
                    case "SMOKE DETECTORS": modifier -= 0.09; break;
                    case "FIRE ALARMS": modifier -= 0.04; break;
                    case "FIRE EXTINGUISHERS": modifier -= 0.01; break;
                    case "SPRINKLER SYSTEM": modifier -= 0.1; break;
                }
            }
            // Slight penalty if none selected
            if (request.getFireSafetySystem().isEmpty()) modifier += 0.1;
        }

        // 3. Proximity to Fire Station
        switch (request.getProximityToFireStation().toUpperCase()) {
            case "LESS4": modifier -= 0.06; break; // Very close
            case "BET48": modifier += 0.18; break;
            case "MORE8": modifier += 0.27; break; // Slight risk
        }

        // 4. Age of Plumbing System
        switch (request.getAgeOfPlumbingSystem().toUpperCase()) {
            case "LESS5": modifier -= 0.06; break;
            case "BET515": modifier += 0.14; break;
            case "MORE15": modifier += 0.25; break;
        }

        // 5. Plumbing Condition
        switch (request.getPlumbingCondition().toUpperCase()) {
            case "FAIR": modifier += 0.26; break;
            case "POOR": modifier += 0.48; break;
        }

        // 6. Flood Risk
        if (Boolean.TRUE.equals(request.getIsFloodProneArea())) {
            modifier += 0.31;
        }

        return modifier;
    }

    private double getTheftModifiers(QuoteRequestDto request) {
        double modifier = 0.4; // Base modifier for theft coverage

        // 1. Security Features (bonuses)
        if (request.getSecurityFeatures() != null) {
            for (String feature : request.getSecurityFeatures()) {
                switch (feature.toUpperCase()) {
                    case "SMART LOCK": modifier -= 0.1; break;
                    case "CCTV": modifier -= 0.05; break;
                    case "GATED ACCESS": modifier -= 0.06; break;
                    case "ALARMS": modifier -= 0.07; break;
                }
            }
            // Slight penalty if no features listed
            if (request.getSecurityFeatures().isEmpty()) modifier += 0.6;
        }

        // 2. Neighborhood Crime Rate
        switch (request.getNeighborhoodCrimeRate().toUpperCase()) {
            case "LOW": modifier += 0.19; break;
            case "MEDIUM": modifier += 0.31; break;
            case "HIGH": modifier += 0.45; break;
        }

        // 3. Previous Incidents
        if (Boolean.TRUE.equals(request.getPreviousIncidents())) {
            modifier += 0.27;
        }else{
            modifier += 0.08;
        }
        return modifier;
    }

    private double getLossModifiers(QuoteRequestDto request) {
        double modifier = 0.6; // Base modifier for Loss of Income coverage

        // 1. Vacancy Rate
        double vacancyRate = calculateVacancyRate(request.getVacantDaysLastYear(), request.getUnitsRentedLastYear());
        System.out.println(vacancyRate);
        if (vacancyRate < 2.0) modifier += 0.11;
        else if (vacancyRate <= 5.0) modifier += 0.32;
        else modifier += 0.49;

        // 2. Number of Tenants
        int tenantCount = request.getNumberOfTenants();
        if (tenantCount <= 0) {
            modifier += 0.5;  // Big penalty if no tenants
        } else {
            int multiple = (tenantCount - 1) / 4; // Integer division
            switch (multiple) {
                case 0 -> modifier += 0.38;   // 1–4 tenants (mild risk)
                case 1 -> modifier += 0.27;   // 5–8 tenants
                case 2 -> modifier += 0.13;   // 9–12 tenants (better distribution)
                default -> modifier -= 0.03;  // 13+ tenants (well diversified)
            }
        }

        // 3. Mortgage Status
        if (Boolean.TRUE.equals(request.getHasMortgage())) {
            modifier += 0.3;  // Slight risk increase
        }else {
            modifier += 0.12;
        }

        return modifier;
    }
}
