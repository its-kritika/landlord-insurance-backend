package com.capstone.landlordInsurance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentReceiptDto {

    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String receiptId;
    private double amount;
    private String status;
    private Long quoteId;
    private LocalDateTime paymentDate;

}
