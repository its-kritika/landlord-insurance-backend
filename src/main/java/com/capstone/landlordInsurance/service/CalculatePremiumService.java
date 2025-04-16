package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.dto.QuoteRequestDto;
import org.springframework.stereotype.Service;

import static com.capstone.landlordInsurance.utils.QuoteUtils.calculateVacancyRate;

@Service
public class CalculatePremiumService {

    public double getPremium(QuoteRequestDto request) {
        double propertyModifier = switch (request.getPropertyType().toUpperCase()) {
            case "RETAIL" -> 0.4;
            case "OFFICE" -> 0.3;
            case "WAREHOUSE" -> 0.7;
            case "INDUSTRIAL" -> 0.9;
            default -> 0.6;
        };

        double firePremium = calculateFirePremium(request, propertyModifier);
        double theftPremium = 2;  //calculateTheftPremium(request, propertyModifier);
        double lossPremium = 1; //calculateLossPremium(request, propertyModifier);

        if ("All-In-One Coverage".equals(request.getCoverageType())) {
            return (firePremium + theftPremium + lossPremium) * 0.85; // Bundle discount
        } else {
            return switch (request.getCoverageType()) {
                case "Fire & Water Damage Coverage" -> firePremium;
                case "Vandalism & Theft Coverage" -> theftPremium;
                case "Loss of Rental Income Coverage" -> lossPremium;
                default -> throw new IllegalArgumentException("Invalid coverage type");
            };
        }
    }

    private double calculateFirePremium(QuoteRequestDto request, double propertyModifier) {
        double baseRate = request.getArea() * 0.6;
        double modifiers = getFireModifiers(request);
        modifiers *= propertyModifier;
        return (request.getPropertyValue() * baseRate / 1000) * modifiers;
    }

    private double calculateTheftPremium(QuoteRequestDto request, double propertyModifier) {
        double baseRate = request.getArea() * 0.3;
        double modifiers = getTheftModifiers(request);
        modifiers *= propertyModifier;
        return (request.getPropertyValue() * baseRate / 1000) * modifiers;
    }

    private double calculateLossPremium(QuoteRequestDto request, double propertyModifier) {
        double baseRate = request.getArea() * 0.8;
        double modifiers = getLossModifiers(request);
        modifiers *= propertyModifier;
        return request.getMonthlyRentalIncome() * 12 * baseRate * modifiers;
    }

    private double getFireModifiers(QuoteRequestDto request) {
        double modifier = 1.0; // Base multiplier

        // 1. Construction Type
        switch (request.getConstructionType().toUpperCase()) {
            case "WOOD": modifier *= 1.5; break;
            case "BRICK": modifier *= 1.0; break;
            case "STEEL": modifier *= 0.8; break;
            case "CONCRETE": modifier *= 0.75; break;
            default: modifier *= 1.05;
        }

        // 2. Fire Safety Systems (cumulative)
        if (request.getFireSafetySystem() != null) {
            for (String system : request.getFireSafetySystem()) {
                switch (system.toUpperCase()) {
                    case "SMOKE DETECTORS": modifier *= 0.85; break;
                    case "FIRE ALARMS": modifier *= 0.82; break;
                    case "FIRE EXTINGUISHERS": modifier *= 0.92; break;
                    case "SPRINKLER SYSTEM": modifier *= 0.6; break;
                    // No system? Handled in next check
                }
            }
            // Penalty if no systems explicitly listed
            if (request.getFireSafetySystem().isEmpty()) modifier *= 1.3;
        }

        // 3. Proximity to Fire Station
        switch (request.getProximityToFireStation().toUpperCase()) {
            case "LESS4": modifier *= 0.8; break;
            case "BET48": modifier *= 1.0; break;
            case "MORE8": modifier *= 1.5; break;
        }
        switch (request.getAgeOfPlumbingSystem().toUpperCase()) {
            case "LESS5": modifier *= 0.5; break;
            case "BET515": modifier *= 0.9; break;
            case "MORE15": modifier *= 1.2; break;
        }
        switch (request.getPlumbingCondition().toUpperCase()){
            case "FAIR": modifier *= 0.4; break;
            case "POOR": modifier *= 0.8; break;
        }
        // 4. Flood Risk
        if (request.getIsFloodProneArea() != null && request.getIsFloodProneArea()) {
            modifier *= 1.4;
        }

        return modifier;
    }

    private double getTheftModifiers(QuoteRequestDto request) {
        double modifier = 1.0;

        // 1. Security Features (cumulative)
        if (request.getSecurityFeatures() != null) {
            for (String feature : request.getSecurityFeatures()) {
                switch (feature.toUpperCase()) {
                    case "SMART LOCK": modifier *= 0.4; break;
                    case "CCTV": modifier *= 0.7; break;
                    case "GATED ACCESS": modifier *= 0.6; break;
                    case "ALARMS": modifier *= 0.8; break;
                }
            }
            // Penalty if no features listed
            if (request.getSecurityFeatures().isEmpty()) modifier *= 1.5;
        }

        // 2. Neighborhood Crime Rate
        switch (request.getNeighborhoodCrimeRate().toUpperCase()) {
            case "LOW": modifier *= 0.8; break;
            case "MEDIUM": modifier *= 1.0; break;
            case "HIGH": modifier *= 1.5; break;
        }

        // 3. Previous Incidents
        if (request.getPreviousIncidents() != null && request.getPreviousIncidents()) {
            modifier *= 1.3;
        }

        return modifier;
    }

    private double getLossModifiers(QuoteRequestDto request) {
        double modifier = 1.0;

        // 1. Vacancy Rate
        double vacancyRate = calculateVacancyRate(request.getVacantDaysLastYear(), request.getUnitsRentedLastYear());

        // 2. Apply Modifier Based on Vacancy Rate
        if (vacancyRate < 10.0) modifier *= 1.0;
        else if (vacancyRate <= 20.0) modifier *= 1.2;
        else modifier *= 1.5;

        // 2. Number of Tenants
        if (request.getNumberOfTenants() <= 0) {
            return 1.5;
        }
        switch (request.getNumberOfTenants()) {
            case 1: modifier *= 1.3; break;  // Single tenant = higher risk
            case 2: modifier *= 1.1; break;
            case 3: modifier *= 1.05; break;
            default: modifier *= 1.0;
        };

        // 3. Mortgage Status
        if (request.getHasMortgage() != null && request.getHasMortgage()) {
            modifier *= 1.2;
        }

        return modifier;
    }
}
