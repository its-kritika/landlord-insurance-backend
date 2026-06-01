package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.dto.PremiumResponseDto;
import com.capstone.landlordInsurance.dto.QuoteRequestDto;
import com.capstone.landlordInsurance.entity.*;
import com.capstone.landlordInsurance.repository.*;
import com.capstone.landlordInsurance.utils.QuoteUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
//        quote.setCalculatedPremium(responseDto.getCalculatedPremium());

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

        Quote quote = getQuoteById(responseDto.getQuoteId());

        Premium premium = new Premium();
//        premium.setQuoteId(responseDto.getQuoteId());
        premium.setQuote(quote);
        premium.setClientName(responseDto.getClientName());
        premium.setClientEmail(responseDto.getClientEmail());
        premium.setCoverageType(responseDto.getCoverageType());
        premium.setBasePremium(responseDto.getBasePremium());
        premium.setCoverageLimit(responseDto.getCoverageLimit());
        premium.setDeductible(responseDto.getDeductible());
        premium.setPropertyValue(responseDto.getPropertyValue());
        premium.setCalculatedPremium(responseDto.getCalculatedPremium());

        // Saving dynamic value to db
        premium.setDynamicVal(responseDto.getDynamicVal());

        premium.setDiscount(responseDto.getDiscount());
        premium.setTax(responseDto.getTax());
        premium.setTime(responseDto.getTime());
        premium.setUpdatedAt(responseDto.getUpdatedAt());
        premium.setStatus(responseDto.getStatus());
        return premium;
    }

    // DTO object which is used by frontend to display values
    public PremiumResponseDto mapToPremiumDto(Premium premium) {
        PremiumResponseDto dto = new PremiumResponseDto();
        dto.setQuoteId(premium.getQuote().getQuoteId());
        dto.setClientName(premium.getClientName());
        dto.setClientEmail(premium.getClientEmail());
        dto.setCoverageType(premium.getCoverageType());
        dto.setBasePremium(premium.getBasePremium());
        dto.setCoverageLimit(premium.getCoverageLimit());
        dto.setDeductible(premium.getDeductible());
        dto.setPropertyValue(premium.getPropertyValue());
        dto.setCalculatedPremium(premium.getCalculatedPremium());
        dto.setDynamicVal(premium.getDynamicVal());
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
//        existingQuote.setCalculatedPremium(responseDto.getCalculatedPremium());
        responseDto.setClientName(client.getName());
        responseDto.setClientEmail(client.getEmail());

        // Save Quote and associated coverage entities
        Quote savedQuote = quoteRepository.save(existingQuote);
        responseDto.setQuoteId(savedQuote.getQuoteId());
        responseDto.setTime(savedQuote.getCreatedAt());
        responseDto.setUpdatedAt(LocalDateTime.now());

        Premium premium = premiumRepository.findByQuoteQuoteId(responseDto.getQuoteId()).get();
        premium.setClientName(responseDto.getClientName());
        premium.setClientEmail(responseDto.getClientEmail());
        premium.setCoverageType(responseDto.getCoverageType());
        premium.setBasePremium(responseDto.getBasePremium());
        premium.setCoverageLimit(responseDto.getCoverageLimit());
        premium.setDeductible(responseDto.getDeductible());
        premium.setPropertyValue(responseDto.getPropertyValue());
        premium.setCalculatedPremium(responseDto.getCalculatedPremium());
        premium.setDynamicVal(responseDto.getDynamicVal());
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
            Premium premium = premiumRepository.findByQuoteQuoteId(id).get();

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

        Premium premium = premiumRepository.findByQuoteQuoteId(id).get();
        quote.setStatus(status);
        premium.setStatus(status);
        quoteRepository.save(quote);
        premiumRepository.save(premium);
    }

    public Page<Quote> getPaginatedQuotesByBrokerId(Long brokerId, Pageable pageable) {
        return quoteRepository.findByBroker_BrokerIdAndStatusNot(brokerId, "deleted", pageable);
    }

    public long countAllQuotesByBrokerId(Long brokerId) {
        return quoteRepository.countByBroker_BrokerIdAndStatusNot(brokerId, "deleted");
    }

    public long countBoundQuotesByBrokerId(Long brokerId){
        return quoteRepository.countByBroker_BrokerIdAndStatus(brokerId, "bound");
    }

//    public Page<Quote> getQuotesByBrokerIdAndStatus(Long brokerId, String status, Pageable pageable) {
//        return quoteRepository.findByBroker_BrokerIdAndStatus(brokerId, status, pageable);
//    }
//
//    public Page<Quote> getQuotesByBrokerIdStatusAndDate(Long brokerId, String status, LocalDateTime startDate, Pageable pageable) {
//        return quoteRepository.findByBroker_BrokerIdAndStatusAndCreatedAtAfter(brokerId, status, startDate, pageable);
//    }
//
//    public Page<Quote> getQuotesByBrokerIdAndDate(Long brokerId, LocalDateTime startDate, Pageable pageable) {
//        return quoteRepository.findByBroker_BrokerIdAndCreatedAtAfter(brokerId, startDate, pageable);
//    }

//    public Page<Quote> searchQuotes(
//            Long brokerId,
//            String search,
//            Pageable pageable
//    ) {
//        return quoteRepository.searchQuotes(brokerId, search, pageable);
//    }

    public Page<Quote> filterQuotes(
            Long brokerId,
            String status,
            LocalDateTime startDate,
            String search,
            Pageable pageable
    ) {

        return quoteRepository.filterQuotes(
                brokerId,
                status,
                startDate,
                search,
                pageable
        );
    }

    public byte[] generateQuotePdf(Long quoteId) {

        BaseColor primaryBlue = new BaseColor(37, 99, 235);

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() ->
                        new RuntimeException("Quote not found"));

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, baos);

            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont( FontFactory.TIMES_ROMAN, 22, primaryBlue);

            Font sectionFont = FontFactory.getFont( FontFactory.HELVETICA_BOLD, 14);

            Font normalFont = FontFactory.getFont( FontFactory.HELVETICA, 11);

            // Title
            Paragraph title = new Paragraph("LANDLORD INSURANCE QUOTE", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(new Paragraph(" "));

            Paragraph brokerName = new Paragraph("Broker: " + quote.getBroker().getName(),
                    FontFactory.getFont( FontFactory.HELVETICA_BOLD, 15, BaseColor.BLACK ));

            brokerName.setAlignment(Element.ALIGN_CENTER);
            document.add(brokerName);
            document.add(new Paragraph(" ")); //empty line

            // Quote Summary Table
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);

            summaryTable.addCell(createHeaderCell("Quote ID"));
            summaryTable.addCell(createBodyCell("QUOTE_" + quote.getQuoteId()));

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd MMM yyyy");

            String formattedDate =
                    quote.getCreatedAt().format(formatter);

            summaryTable.addCell(createHeaderCell("Generated On"));
            summaryTable.addCell(createBodyCell(formattedDate));

            summaryTable.addCell(createHeaderCell("Status"));
            summaryTable.addCell(createBodyCell(quote.getStatus()));

            document.add(summaryTable);

            document.add(new Paragraph(" "));

            // Client Details Section
            Paragraph clientHeading = new Paragraph("Client Details", sectionFont);
            Client client = quote.getClient();

            clientHeading.setSpacingAfter(5f); // for dynamic spacing in next line
            document.add(clientHeading);

            PdfPTable clientTable = new PdfPTable(2);

            clientTable.setWidthPercentage(100);

            clientTable.addCell(createHeaderCell("Client Name"));
            clientTable.addCell( createBodyCell(String.valueOf( client.getName() )));

            clientTable.addCell(createHeaderCell("Client Email"));
            clientTable.addCell( createBodyCell(String.valueOf( client.getEmail() )));

            clientTable.addCell(createHeaderCell("Residential Address"));
            clientTable.addCell( createBodyCell(String.valueOf( client.getAddress() )));

            clientTable.addCell(createHeaderCell("Contact Details"));
            clientTable.addCell( createBodyCell(String.valueOf( client.getPhone() )));

            document.add(clientTable);

            document.add(new Paragraph(" "));

            // Property Section
            Paragraph propertyHeading = new Paragraph("PROPERTY INFORMATION", sectionFont);

            propertyHeading.setSpacingAfter(5f);
            document.add(propertyHeading);
//            document.add(new Paragraph(" "));

            PdfPTable propertyTable = new PdfPTable(2);

            propertyTable.setWidthPercentage(100);

            propertyTable.addCell(createHeaderCell("Property Address"));
            propertyTable.addCell( createBodyCell(String.valueOf( quote.getPropertyAddress() )));

            propertyTable.addCell(createHeaderCell("ZIP Code"));
            propertyTable.addCell( createBodyCell(String.valueOf( quote.getPropertyZip() )));

            propertyTable.addCell(createHeaderCell("Property Type"));
            propertyTable.addCell( createBodyCell(String.valueOf( quote.getPropertyType() )));

            propertyTable.addCell(createHeaderCell("Property Value"));
            propertyTable.addCell(createBodyCell("₹" + quote.getPropertyValue()) );

            propertyTable.addCell(createHeaderCell("Year Built"));
            propertyTable.addCell( createBodyCell(String.valueOf( quote.getYearBuilt() )));

            propertyTable.addCell(createHeaderCell("Area in sq ft"));
            propertyTable.addCell( createBodyCell(String.valueOf(quote.getArea())));

            document.add(propertyTable);

            document.add(new Paragraph(" "));

            // Coverage Section
            Paragraph coverageHeading = new Paragraph("COVERAGE INFORMATION", sectionFont);

            coverageHeading.setSpacingAfter(5f);
            document.add(coverageHeading);

            PdfPTable coverageTable = new PdfPTable(2);

            coverageTable.setWidthPercentage(100);

            coverageTable.addCell(createHeaderCell("Coverage Type"));
            coverageTable.addCell( createBodyCell(String.valueOf( quote.getCoverageType() )));

            coverageTable.addCell(createHeaderCell("Coverage Limit"));
            coverageTable.addCell(createBodyCell("₹" + quote.getCoverageLimit()));

            coverageTable.addCell(createHeaderCell("Deductible"));
            coverageTable.addCell(createBodyCell("₹" + quote.getDeductibleValue()));

            document.add(coverageTable);

            document.add(new Paragraph(" "));

            // Fire & Water Details
            if ("Fire & Water Damage Coverage".equalsIgnoreCase(
                    quote.getCoverageType()) || "All-In-One Coverage".equalsIgnoreCase(
                    quote.getCoverageType())) {

                FireWaterCov fireWater = quote.getFireWaterCoverage();

                Paragraph fireWaterHeading = new Paragraph("FIRE & WATER COVERAGE DETAILS", sectionFont);

                fireWaterHeading.setSpacingAfter(5f);
                document.add(fireWaterHeading);

                PdfPTable fireTable = new PdfPTable(2);

                fireTable.setWidthPercentage(100);

                fireTable.addCell(createHeaderCell("Construction Type"));
                fireTable.addCell( createBodyCell(String.valueOf( fireWater.getConstructionType())));

                String fireStationDistance = switch (fireWater.getProximityToFireStation()) {
                    case "less4" -> "Less than 4 km";
                    case "bet48" -> "Between 4 km and 8 km";
                    case "more8" -> "More than 8 km";
                    default -> fireWater.getProximityToFireStation();
                };

                fireTable.addCell(createHeaderCell("Fire Station Distance"));
                fireTable.addCell(createBodyCell(fireStationDistance));

                fireTable.addCell(createHeaderCell("Plumbing Condition"));
                fireTable.addCell( createBodyCell(String.valueOf( fireWater.getPlumbingCondition() )));

                String plumbingAge = switch (fireWater.getAgeOfPlumbingSystem()) {
                    case "less5" -> "Less than 5 years";
                    case "bet515" -> "Between 5 to 15 years";
                    case "more15" -> "More than 15 years";
                    default -> fireWater.getProximityToFireStation();
                };

                fireTable.addCell(createHeaderCell("Plumbing Age"));
                fireTable.addCell( createBodyCell(String.valueOf( plumbingAge )));

                fireTable.addCell(createHeaderCell("Flood Prone Area"));
                fireTable.addCell(createBodyCell(fireWater.isInFloodProneArea() ? "Yes" : "No"));

                List<String> safetySystems = getFireWaterStrings(fireWater);

                String fireSafetyText = safetySystems.isEmpty()
                        ? "None"
                        : String.join(", ", safetySystems);

                fireTable.addCell(createHeaderCell("Fire Safety Systems"));
                fireTable.addCell(createBodyCell(fireSafetyText));

                document.add(fireTable);

                document.add(new Paragraph(" "));
            }

            // Theft Coverage Details
            if ("Vandalism & Theft Coverage".equalsIgnoreCase(
                    quote.getCoverageType()) || "All-In-One Coverage".equalsIgnoreCase(
                    quote.getCoverageType())) {

                VandalismTheftCov vandalismTheft = quote.getVandalismTheftCoverage();
                Paragraph theftHeading = new Paragraph("THEFT & VANDALISM DETAILS", sectionFont);

                theftHeading.setSpacingAfter(5f);
                document.add(theftHeading);

                PdfPTable theftTable = new PdfPTable(2);

                theftTable.setWidthPercentage(100);

                theftTable.addCell(createHeaderCell("Crime Rate"));
                theftTable.addCell( createBodyCell(String.valueOf( vandalismTheft.getNeighCrimeRate()) ));

                theftTable.addCell(createHeaderCell("Previous Incidents"));
                theftTable.addCell( createBodyCell(vandalismTheft.isHadPreviousIncidents() ? "Yes" : "No"));

                List<String> securityFeatures = getTheftStrings(vandalismTheft);

                String fireSafetyText = securityFeatures.isEmpty()
                        ? "None"
                        : String.join(", ", securityFeatures);

                theftTable.addCell(createHeaderCell("Security Features"));
                theftTable.addCell(createBodyCell(fireSafetyText));

                document.add(theftTable);

                document.add(new Paragraph(" "));
            }

            // Loss Of Income
            if ("Loss of Rental Income Coverage".equalsIgnoreCase(
                    quote.getCoverageType()) || "All-In-One Coverage".equalsIgnoreCase(
                    quote.getCoverageType())) {

                LossOfIncomeCov lossOfIncome = quote.getLossOfIncomeCoverage();

                Paragraph losInHeading = new Paragraph("LOSS OF RENTAL INCOME DETAILS", sectionFont);

                losInHeading.setSpacingAfter(5f);
                document.add(losInHeading);

                PdfPTable incomeTable = new PdfPTable(2);

                incomeTable.setWidthPercentage(100);

                incomeTable.addCell(createHeaderCell("Monthly Rental Income"));
                incomeTable.addCell(createBodyCell("₹" + lossOfIncome.getMonthlyRentalIncome()));

                incomeTable.addCell(createHeaderCell("Number of Tenants"));
                incomeTable.addCell( createBodyCell(String.valueOf( lossOfIncome.getNumberOfTenants()) ));

                incomeTable.addCell(createHeaderCell("Number of Units Rented"));
                incomeTable.addCell( createBodyCell(String.valueOf( lossOfIncome.getUnitsRented()) ));

                incomeTable.addCell(createHeaderCell("Number of Vacant Days in Past Year"));
                incomeTable.addCell( createBodyCell(String.valueOf( lossOfIncome.getVacantDaysPastYear() )));

                incomeTable.addCell(createHeaderCell("Mortgage on Property"));
                incomeTable.addCell( createBodyCell(lossOfIncome.isHasMortgage() ? "Yes" : "No"));

                document.add(incomeTable);

                document.add(new Paragraph(" "));
            }

            // Calculated Premium Section
            Premium premium = quote.getPremium();
            Paragraph premiumHeading = new Paragraph("PREMIUM GENERATED", sectionFont);

            premiumHeading.setSpacingAfter(5f);
            document.add(premiumHeading);

            PdfPTable premiumTable = new PdfPTable(2);

            premiumTable.setWidthPercentage(100);

            premiumTable.addCell(createHeaderCell("Base Amount"));
            premiumTable.addCell( createBodyCell(String.valueOf( premium.getBasePremium() )));

            premiumTable.addCell(createHeaderCell("Dynamic Ad-Ons"));
            premiumTable.addCell( createBodyCell(String.valueOf( premium.getDynamicVal() )));

            premiumTable.addCell(createHeaderCell("Total Premium"));
            premiumTable.addCell( createBodyCell(String.valueOf( premium.getCalculatedPremium() )));

            document.add(premiumTable);

            document.add(new Paragraph(" "));

            // Footer
            Font underlineFont = new Font(normalFont);
            underlineFont.setStyle(Font.UNDERLINE);

            Paragraph footer =
                    new Paragraph("Generated by Landlord Insurance Broker Portal", underlineFont);

            footer.setSpacingAfter(5f);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            Paragraph footer2 =
                    new Paragraph("By LandSure", FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            11));

            footer2.setAlignment(Element.ALIGN_CENTER);
            document.add(footer2);

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error generating PDF",
                    e);
        }
    }

    @Nonnull
    private static List<String> getFireWaterStrings(FireWaterCov fireWater) {
        List<String> safetySystems = new ArrayList<>();

        if (fireWater.isHasFireAlarms()) {
            safetySystems.add("Fire Alarm");
        }

        if (fireWater.isHasSmokeDetectors()) {
            safetySystems.add("Smoke Detector");
        }

        if (fireWater.isHasSprinklerSystem()) {
            safetySystems.add("Sprinkler System");
        }

        if (fireWater.isHasFireExtinguishers()) {
            safetySystems.add("Fire Extinguisher");
        }
        return safetySystems;
    }

    @Nonnull
    private static List<String> getTheftStrings(VandalismTheftCov vandalismTheftCov) {
        List<String> securityFeatures = new ArrayList<>();

        if (vandalismTheftCov.isHasAlarms()) {
            securityFeatures.add("Alarm");
        }

        if (vandalismTheftCov.isHasCCTV()) {
            securityFeatures.add("CCTV");
        }

        if (vandalismTheftCov.isHasSmartLock()) {
            securityFeatures.add("Smart Lock");
        }

        if (vandalismTheftCov.isHasGatedAccess()) {
            securityFeatures.add("Gated Access");
        }
        return securityFeatures;

    }

    private PdfPCell createHeaderCell(String text) {

        BaseColor primaryBlue = new BaseColor(37, 99, 235);

        PdfPCell cell = new PdfPCell(
                new Phrase( text,
                        FontFactory.getFont( FontFactory.HELVETICA_BOLD, 11, primaryBlue)
                )
        );
        cell.setPadding(8);

        return cell;
    }

    private PdfPCell createBodyCell(String text) {

        PdfPCell cell = new PdfPCell(
                new Phrase( text,
                        FontFactory.getFont( FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK)
                )
        );
        cell.setPadding(8);

        return cell;
    }
}
