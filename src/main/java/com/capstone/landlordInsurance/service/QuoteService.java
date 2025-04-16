package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.dto.QuoteRequestDto;
import com.capstone.landlordInsurance.entity.*;
import com.capstone.landlordInsurance.repository.*;
import com.capstone.landlordInsurance.utils.QuoteUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class QuoteService {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private FireWaterRepository fireWaterRepository;

    @Autowired
    private VandalismTheftRepository vandalismTheftRepository;

    @Autowired
    private LossOfIncomeRepository lossOfIncomeRepository;

    @Autowired
    private CalculatePremiumService premiumService;

    @Transactional
    public Quote createQuote(QuoteRequestDto quoteRequestDTO) {
        // Create Quote Entity and set values from DTO
        Broker broker = brokerRepository.findById(quoteRequestDTO.getBrokerId())
                .orElseThrow(() -> new RuntimeException("Broker not found with id: " + quoteRequestDTO.getBrokerId()));

        Client client = clientRepository.findById(quoteRequestDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + quoteRequestDTO.getClientId()));

        Quote quote = new Quote();

        QuoteUtils.updateQuoteFields(quote, quoteRequestDTO, client);
        quote.setBroker(broker);
        System.out.println(quoteRequestDTO.getCoverageType());

        // Handle Coverage Logic
        if ("Fire & Water Damage Coverage".equals(quoteRequestDTO.getCoverageType()) ||
                "All-In-One Coverage".equals(quoteRequestDTO.getCoverageType()))  {
            // Map fire/water coverage data
            FireWaterCov fireWaterCov = new FireWaterCov();
            QuoteUtils.updateFireWaterCoverage(fireWaterCov, quoteRequestDTO);

            fireWaterCov.setQuote(quote);
            quote.setFireWaterCoverage(fireWaterCov);
        }

        if ("Vandalism & Theft Coverage".equals(quoteRequestDTO.getCoverageType()) ||
                "All-In-One Coverage".equals(quoteRequestDTO.getCoverageType())) {
            // Map vandalism/theft coverage data
            VandalismTheftCov vandalismTheftCov = new VandalismTheftCov();

            QuoteUtils.updateVandalismTheftCoverage(vandalismTheftCov, quoteRequestDTO);

            vandalismTheftCov.setQuote(quote);
            quote.setVandalismTheftCoverage(vandalismTheftCov);
        }

        if ("Loss of Rental Income Coverage".equals(quoteRequestDTO.getCoverageType()) ||
                "All-In-One Coverage".equals(quoteRequestDTO.getCoverageType())) {
            // Map loss of income coverage data
            LossOfIncomeCov lossOfIncomeCov = new LossOfIncomeCov();

            QuoteUtils.updateLossOfIncomeCoverage(lossOfIncomeCov, quoteRequestDTO);

            lossOfIncomeCov.setQuote(quote);
            quote.setLossOfIncomeCoverage(lossOfIncomeCov);
        }

        quote.setCalculatedPremium(premiumService.getPremium(quoteRequestDTO));

        // Save Quote and associated coverage entities
        return quoteRepository.save(quote);
    }

    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    public Quote getQuoteById(Long id) {
        return quoteRepository.findById(id).orElse(null);
    }

    @Transactional
    public Quote updateQuoteById(Long id, QuoteRequestDto updatedQuote) {

        Optional<Quote> optionalQuote = quoteRepository.findById(id);
        if (optionalQuote.isEmpty()) return null;

        Client client = clientRepository.findById(updatedQuote.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + updatedQuote.getClientId()));


        Quote existingQuote = optionalQuote.get();
        QuoteUtils.updateQuoteFields(existingQuote, updatedQuote, client);

        String coverageType = updatedQuote.getCoverageType() != null
                ? updatedQuote.getCoverageType()
                : existingQuote.getCoverageType();


        // Remove existing coverage types that don't match the selected one
        if (!"Fire & Water Damage Coverage".equals(coverageType) && !"All-In-One Coverage".equals(coverageType)) {
            if (existingQuote.getFireWaterCoverage() != null) {
                fireWaterRepository.delete(existingQuote.getFireWaterCoverage());
                existingQuote.setFireWaterCoverage(null);
            }
        }

        if (!"Vandalism & Theft Coverage".equals(coverageType) && !"All-In-One Coverage".equals(coverageType)) {
            if (existingQuote.getVandalismTheftCoverage() != null) {
                vandalismTheftRepository.delete(existingQuote.getVandalismTheftCoverage());
                existingQuote.setVandalismTheftCoverage(null);
            }
        }

        if (!"Loss of Rental Income Coverage".equals(coverageType) && !"All-In-One Coverage".equals(coverageType)) {
            if (existingQuote.getLossOfIncomeCoverage() != null) {
                lossOfIncomeRepository.delete(existingQuote.getLossOfIncomeCoverage());
                existingQuote.setLossOfIncomeCoverage(null);
            }
        }

        // Add/update selected coverage data
        if ("Fire & Water Damage Coverage".equals(coverageType) || "All-In-One Coverage".equals(coverageType)) {
            FireWaterCov fireWaterCov = existingQuote.getFireWaterCoverage();

            if (fireWaterCov == null) {
                fireWaterCov = new FireWaterCov();
                fireWaterCov.setQuote(existingQuote); // important to set the relationship
                existingQuote.setFireWaterCoverage(fireWaterCov);
            }
            QuoteUtils.updateFireWaterCoverage(fireWaterCov, updatedQuote);
        }

        if ("Vandalism & Theft Coverage".equals(coverageType) || "All-In-One Coverage".equals(coverageType)) {
            VandalismTheftCov vandalismTheftCov = existingQuote.getVandalismTheftCoverage();

            if (vandalismTheftCov == null) {
                vandalismTheftCov = new VandalismTheftCov();
                vandalismTheftCov.setQuote(existingQuote);
                existingQuote.setVandalismTheftCoverage(vandalismTheftCov);
            }
            QuoteUtils.updateVandalismTheftCoverage(vandalismTheftCov, updatedQuote);
        }

        if ("Loss of Rental Income Coverage".equals(coverageType) || "All-In-One Coverage".equals(coverageType)) {
            LossOfIncomeCov lossOfIncomeCov = existingQuote.getLossOfIncomeCoverage();
            if (lossOfIncomeCov == null) {
                lossOfIncomeCov = new LossOfIncomeCov();
                lossOfIncomeCov.setQuote(existingQuote);
                existingQuote.setLossOfIncomeCoverage(lossOfIncomeCov);
            }

            QuoteUtils.updateLossOfIncomeCoverage(lossOfIncomeCov, updatedQuote);
        }

        return quoteRepository.save(existingQuote);
    }

    public boolean deleteQuote(Long id) {
        if (quoteRepository.existsById(id)) {
            quoteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
