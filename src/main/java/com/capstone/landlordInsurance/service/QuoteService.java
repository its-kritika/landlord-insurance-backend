package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.dto.PremiumResponseDto;
import com.capstone.landlordInsurance.dto.QuoteRequestDto;
import com.capstone.landlordInsurance.entity.*;
import com.capstone.landlordInsurance.repository.*;
import com.capstone.landlordInsurance.utils.QuoteUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Autowired
    private PremiumRepository premiumRepository;

    @Transactional
    public PremiumResponseDto createQuote(QuoteRequestDto quoteRequestDTO, String brokerEmail) {
        // Create Quote Entity and set values from DTO
        Broker broker = brokerRepository.findByEmail(brokerEmail);
        if(broker == null){
            throw new RuntimeException("Broker not found");
        }
        Client client = clientRepository.findById(quoteRequestDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

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

        PremiumResponseDto responseDto = premiumService.getPremium(quoteRequestDTO);
        responseDto.setClientName(client.getName());
        responseDto.setClientEmail(client.getEmail());
        quote.setCalculatedPremium(responseDto.getCalculatedPremium());

        // Save Quote and associated coverage entities
        Quote savedQuote = quoteRepository.save(quote);
        responseDto.setQuoteId(savedQuote.getQuoteId());
        responseDto.setTime(savedQuote.getCreatedAt());
        responseDto.setUpdatedAt(savedQuote.getCreatedAt());
        responseDto.setStatus(savedQuote.getStatus());

        Premium premium = mapToPremiumEntity(responseDto);
        premiumRepository.save(premium);

        return responseDto;
    }

    private Premium mapToPremiumEntity(PremiumResponseDto responseDto) {
        Premium premium = new Premium();
        premium.setQuoteId(responseDto.getQuoteId());
        premium.setClientName(responseDto.getClientName());
        premium.setClientEmail(responseDto.getClientEmail());
        premium.setCoverageType(responseDto.getCoverageType());
        premium.setBasePremium(responseDto.getBasePremium());
        premium.setCoverageLimit(responseDto.getCoverageLimit());
        premium.setDeductible(responseDto.getDeductible());
        premium.setPropertyValue(responseDto.getPropertyValue());
        premium.setCalculatedPremium(responseDto.getCalculatedPremium());
        premium.setDiscount(responseDto.getDiscount());
        premium.setTax(responseDto.getTax());
        premium.setTime(responseDto.getTime());
        premium.setUpdatedAt(responseDto.getUpdatedAt());
        premium.setStatus(responseDto.getStatus());
        return premium;
    }

    public PremiumResponseDto mapToPremiumDto(Premium premium) {
        PremiumResponseDto dto = new PremiumResponseDto();
        dto.setQuoteId(premium.getQuoteId());
        dto.setClientName(premium.getClientName());
        dto.setClientEmail(premium.getClientEmail());
        dto.setCoverageType(premium.getCoverageType());
        dto.setBasePremium(premium.getBasePremium());
        dto.setCoverageLimit(premium.getCoverageLimit());
        dto.setDeductible(premium.getDeductible());
        dto.setPropertyValue(premium.getPropertyValue());
        dto.setCalculatedPremium(premium.getCalculatedPremium());
        dto.setDiscount(premium.getDiscount());
        dto.setTax(premium.getTax());
        dto.setTime(premium.getTime());
        dto.setUpdatedAt(premium.getUpdatedAt());
        dto.setStatus(premium.getStatus());
        return dto;
    }

    public List<Quote> getQuotesByBrokerId(Long brokerId){
        return quoteRepository.findByBroker_BrokerId(brokerId);
    }

    public Quote getQuoteById(Long id) {
        return quoteRepository.findById(id).orElse(null);
    }

    @Transactional
    public PremiumResponseDto updateQuoteById(Long id, QuoteRequestDto updatedQuote) {

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

        PremiumResponseDto responseDto = premiumService.getPremium(updatedQuote);
        existingQuote.setCalculatedPremium(responseDto.getCalculatedPremium());
        responseDto.setClientName(client.getName());
        responseDto.setClientEmail(client.getEmail());

        // Save Quote and associated coverage entities
        Quote savedQuote = quoteRepository.save(existingQuote);
        responseDto.setQuoteId(savedQuote.getQuoteId());
        responseDto.setTime(savedQuote.getCreatedAt());
        responseDto.setUpdatedAt(LocalDateTime.now());

        Premium premium = premiumRepository.findByQuoteId(responseDto.getQuoteId()).get();
        premium.setClientName(responseDto.getClientName());
        premium.setClientEmail(responseDto.getClientEmail());
        premium.setCoverageType(responseDto.getCoverageType());
        premium.setBasePremium(responseDto.getBasePremium());
        premium.setCoverageLimit(responseDto.getCoverageLimit());
        premium.setDeductible(responseDto.getDeductible());
        premium.setPropertyValue(responseDto.getPropertyValue());
        premium.setCalculatedPremium(responseDto.getCalculatedPremium());
        premium.setDiscount(responseDto.getDiscount());
        premium.setTax(responseDto.getTax());
        premium.setUpdatedAt(responseDto.getUpdatedAt());
        premium.setStatus(savedQuote.getStatus());
        premiumRepository.save(premium);

        return responseDto;
    }

    @Transactional
    public boolean deleteQuote(Long id) {
        Optional<Quote> optionalQuote = quoteRepository.findById(id);
        if (optionalQuote.isPresent()) {
            Quote quote = optionalQuote.get();
            Premium premium = premiumRepository.findByQuoteId(id).get();

            if (quote.getFireWaterCoverage() != null) {
                quote.getFireWaterCoverage().setQuote(null);
            }
            if (quote.getVandalismTheftCoverage() != null) {
                quote.getVandalismTheftCoverage().setQuote(null);
            }
            if (quote.getLossOfIncomeCoverage() != null) {
                quote.getLossOfIncomeCoverage().setQuote(null);
            }

            premiumRepository.delete(premium);
            quoteRepository.delete(quote);
            return true;
        }
        return false;
    }

    @Transactional
    public void updateQuoteStatus(Long id, String status) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        Premium premium = premiumRepository.findByQuoteId(id).get();
        quote.setStatus(status);
        premium.setStatus(status);
        quoteRepository.save(quote);
        premiumRepository.save(premium);
    }

}
