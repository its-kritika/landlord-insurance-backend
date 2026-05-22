package com.capstone.landlordInsurance.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.test.api.key}")
    private String apiKey;

    @Value("${razorpay.test.api.secret}")
    private String apiSecret;

    public String createOrder(int amount, String currency, String recipientId) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(apiKey, apiSecret);
        return "";
    }
}
