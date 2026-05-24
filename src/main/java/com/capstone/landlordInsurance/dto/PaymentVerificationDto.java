package com.capstone.landlordInsurance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentVerificationDto {

    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
    private Long quoteId;

}
