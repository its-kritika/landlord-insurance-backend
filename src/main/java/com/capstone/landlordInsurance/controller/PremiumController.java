package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.dto.PremiumResponseDto;
import com.capstone.landlordInsurance.dto.QuoteRequestDto;
import com.capstone.landlordInsurance.service.CalculatePremiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/premium")
public class PremiumController {

    @Autowired
    private CalculatePremiumService premiumService;

    // Constructor injection
//    public PremiumController(PremiumCalculationService premiumService) {
//        this.premiumService = premiumService;
//    }

    @PostMapping()
    public ResponseEntity<?> calculatePremium(
            @RequestBody QuoteRequestDto request) {

        PremiumResponseDto responseDto = premiumService.getPremium(request);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
