package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.dto.PaymentReceiptDto;
import com.capstone.landlordInsurance.dto.PaymentRequestDto;
import com.capstone.landlordInsurance.dto.PaymentVerificationDto;
import com.capstone.landlordInsurance.entity.Quote;
import com.capstone.landlordInsurance.service.EmailService;
import com.capstone.landlordInsurance.service.QuoteService;
import com.capstone.landlordInsurance.service.RazorpayService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("quote/payment")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private QuoteService quoteService;

    @PostMapping("create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentRequestDto paymentRequestDto) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();
            String order = razorpayService.createOrder(paymentRequestDto, brokerEmail);
            return ResponseEntity.ok(order);

        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDto paymentVerificationDto) {

        Map<String, String> response = new HashMap<>();
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();
            razorpayService.verifyPayment(paymentVerificationDto, brokerEmail);

            Quote quote = quoteService.getQuoteById(paymentVerificationDto.getQuoteId());
            byte[] pdfBytes = quoteService.generateQuotePdf(quote.getQuoteId());

            //Sending mail to client with attachment
            emailService.sendEmailWithAttachment(
                    quote.getClient().getEmail(),
                    "LandSure: Your Landlord Insurance Policy has been Issued",
                    "Dear " + quote.getClient().getName() + ",\n" +
                            "\n" +
                            "Your insurance policy has been successfully generated.\n" +
                            "\n" +
                            "Quote ID: " + quote.getQuoteId() +
                            "\nCoverage: " + quote.getCoverageType() +
                            "\nPremium: ₹" + quote.getPremium().getCalculatedPremium() + "/year" +

                            "\n" +
                            "\nPlease refer to the attached quote details below.\n" +
                            "\n" +
                            "Thank you,\n" +
                            "LandSure",
                    pdfBytes,
                    "Quote-" +
                            quote.getQuoteId() +
                            ".pdf"
            );

            //Sending mail to broker with attachment
            emailService.sendEmailWithAttachment(
                    brokerEmail,
                    "LandSure: Policy has been successfully generated",
                    "Dear " + quote.getBroker().getName() + ",\n" +
                            "\nQuote bound successfully. Below are the details attached.\n" +
                            "\n" +
                            "Quote ID: " + quote.getQuoteId() +
                            "\nClient: " + quote.getClient().getName() +
                            "\nProperty: " + quote.getPropertyAddress() +
                            "\nPremium: ₹" + quote.getPremium().getCalculatedPremium() + "/year" +
                            "\n" +
                            "\nPlease refer to the attached quote details below.\n" +
                            "\n" +
                            "Thank you,\n" +
                            "LandSure",
                    pdfBytes,
                    "Quote-" +
                            quote.getQuoteId() +
                            ".pdf"
            );

            //Sending mail without attachment
//            emailService.sendEmail(
//                    quote.getClient().getEmail(),
//                    "LandSure: Your Landlord Insurance Policy has been Issued",
//                    "body"
//            );
//
//            emailService.sendEmail(
//                    brokerEmail,
//                    "LandSure: Policy has been successfully generated",
//                    "body"
//            );

            response.put("message", "Payment has been verified!");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {

            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/receipt/{quoteId}")
    public ResponseEntity<?> getReceipt(@PathVariable Long quoteId) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();

            PaymentReceiptDto paymentReceiptDto = razorpayService.getPaymentDetails(quoteId, brokerEmail);

            return new ResponseEntity<>(paymentReceiptDto, HttpStatus.OK);
        } catch (Exception e){

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage() != null ? e.getMessage() : "Payment Details could not be fetched! Try again later.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping("/download/{quoteId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long quoteId) {

        byte[] pdf = razorpayService.generatePaymentPdf(quoteId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Payment_" + quoteId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
