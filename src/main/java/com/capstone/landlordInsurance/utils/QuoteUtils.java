package com.capstone.landlordInsurance.utils;

import com.capstone.landlordInsurance.dto.QuoteRequestDto;
import com.capstone.landlordInsurance.entity.*;

import java.util.List;

public class QuoteUtils {

    public static void updateQuoteFields(Quote existingQuote, QuoteRequestDto updatedQuote, Client client) {
        // Only update fields if values are not null
        if (updatedQuote.getCoverageType() != null) existingQuote.setCoverageType(updatedQuote.getCoverageType());
        if (client != null) existingQuote.setClient(client);
        if (updatedQuote.getPropertyAddress() != null) existingQuote.setPropertyAddress(updatedQuote.getPropertyAddress());
        if (updatedQuote.getPropertyZip() != null) existingQuote.setPropertyZip(updatedQuote.getPropertyZip());
        if (updatedQuote.getPropertyType() != null) existingQuote.setPropertyType(updatedQuote.getPropertyType());
        if (updatedQuote.getPropertyValue() != null) existingQuote.setPropertyValue(updatedQuote.getPropertyValue());
        if (updatedQuote.getYearBuilt() != null) existingQuote.setYearBuilt(updatedQuote.getYearBuilt());
        if (updatedQuote.getArea() != null) existingQuote.setArea(updatedQuote.getArea());
    }

    public static void updateFireWaterCoverage(FireWaterCov fireWaterCov, QuoteRequestDto updatedQuote) {
        List<String> fireSafety = updatedQuote.getFireSafetySystem();
        if (fireSafety != null) {
            fireWaterCov.setHasSmokeDetectors(fireSafety.contains("Smoke Detectors"));
            fireWaterCov.setHasFireAlarms(fireSafety.contains("Fire Alarms"));
            fireWaterCov.setHasFireExtinguishers(fireSafety.contains("Fire Extinguishers"));
            fireWaterCov.setHasSprinklerSystem(fireSafety.contains("Sprinkler System"));
        }

        if (updatedQuote.getConstructionType() != null) fireWaterCov.setConstructionType(updatedQuote.getConstructionType());
        if (updatedQuote.getAgeOfPlumbingSystem() != null) fireWaterCov.setAgeOfPlumbingSystem(updatedQuote.getAgeOfPlumbingSystem());
        if (updatedQuote.getPlumbingCondition() != null) fireWaterCov.setPlumbingCondition(updatedQuote.getPlumbingCondition());
        if (updatedQuote.getIsFloodProneArea() != null) fireWaterCov.setInFloodProneArea(updatedQuote.getIsFloodProneArea());
        if (updatedQuote.getProximityToFireStation() != null) fireWaterCov.setProximityToFireStation(updatedQuote.getProximityToFireStation());
    }

    public static void updateVandalismTheftCoverage(VandalismTheftCov vandalismTheftCov, QuoteRequestDto updatedQuote) {
        List<String> theftSafety = updatedQuote.getSecurityFeatures();
        if (theftSafety != null) {
            vandalismTheftCov.setHasAlarms(theftSafety.contains("Alarms"));
            vandalismTheftCov.setHasCCTV(theftSafety.contains("CCTV"));
            vandalismTheftCov.setHasGatedAccess(theftSafety.contains("Gated Access"));
            vandalismTheftCov.setHasSmartLock(theftSafety.contains("Smart Lock"));
        }

        if (updatedQuote.getNeighborhoodCrimeRate() != null) vandalismTheftCov.setNeighCrimeRate(updatedQuote.getNeighborhoodCrimeRate());
        if (updatedQuote.getPreviousIncidents() != null) vandalismTheftCov.setHadPreviousIncidents(updatedQuote.getPreviousIncidents());
    }

    public static void updateLossOfIncomeCoverage(LossOfIncomeCov lossOfIncomeCov, QuoteRequestDto updatedQuote) {
        if (updatedQuote.getMonthlyRentalIncome() != null) lossOfIncomeCov.setMonthlyRentalIncome(updatedQuote.getMonthlyRentalIncome());
        if (updatedQuote.getNumberOfTenants() != null) lossOfIncomeCov.setNumberOfTenants(updatedQuote.getNumberOfTenants());
        if (updatedQuote.getHasMortgage() != null) lossOfIncomeCov.setHasMortgage(updatedQuote.getHasMortgage());

        if (updatedQuote.getVacantDaysLastYear() != null && updatedQuote.getUnitsRentedLastYear() != null) {
            double vacancyRate = calculateVacancyRate(
                    updatedQuote.getVacantDaysLastYear(),
                    updatedQuote.getUnitsRentedLastYear()
            );
            lossOfIncomeCov.setVacancyRate(vacancyRate);
        }
    }

    public static double calculateVacancyRate(int totalVacantDays, int totalUnits) {
        if (totalUnits == 0) return 0.0;
        double totalAvailableDays = totalUnits * 365.0;
        return (totalVacantDays / totalAvailableDays) * 100.0;
    }
}

