package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.dto.PaymentRequestDto;
import com.capstone.landlordInsurance.service.RazorpayService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PostMapping("create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentRequestDto paymentRequestDto) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();
            String order = razorpayService.createOrder(paymentRequestDto, brokerEmail);
            return ResponseEntity.ok(order);

        } catch (Exception e){

            Map<String, String> response = new HashMap<>();
            String msg = e.getMessage() != null ? e.getMessage() : "Payment failed!";
            response.put("error", msg);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }
}
