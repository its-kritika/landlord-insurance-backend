package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.dto.PaymentReceiptDto;
import com.capstone.landlordInsurance.dto.PaymentRequestDto;
import com.capstone.landlordInsurance.dto.PaymentVerificationDto;
import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.entity.Payment;
import com.capstone.landlordInsurance.entity.Quote;
import com.capstone.landlordInsurance.repository.BrokerRepository;
import com.capstone.landlordInsurance.repository.PaymentRepository;
import com.capstone.landlordInsurance.repository.QuoteRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class RazorpayService {

    @Value("${razorpay.test.api.key}")
    private String apiKey;

    @Value("${razorpay.test.api.secret}")
    private String apiSecret;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private QuoteService quoteService;

    @Transactional
    public String createOrder(PaymentRequestDto paymentRequestDto, String brokerEmail) throws RazorpayException {

        Broker broker = brokerRepository.findByEmail(brokerEmail);
        if(broker == null){
            throw new RuntimeException("Broker not found");
        }

        RazorpayClient razorpayClient = new RazorpayClient(apiKey, apiSecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", paymentRequestDto.getAmount() * 100); // By default, amount would be in paise so need to convert in Rs by multiplying it by 100
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "Quote_" + paymentRequestDto.getQuoteId());

        Order order = razorpayClient.orders.create(orderRequest);

        Payment payment = new Payment();

        payment.setAmount(paymentRequestDto.getAmount());

        payment.setRazorpayOrderId(order.get("id"));

        payment.setReceiptId("Quote_" + paymentRequestDto.getQuoteId());

        Quote quote = quoteRepository.findById(
                paymentRequestDto.getQuoteId()
        ).orElseThrow();

        payment.setQuote(quote);
        paymentRepository.save(payment);
        return order.toString();
    }

    @Transactional
    public void verifyPayment(PaymentVerificationDto dto, String brokerEmail) throws Exception {

        Broker broker = brokerRepository.findByEmail(brokerEmail);
        if(broker == null){
            throw new RuntimeException("Broker not found");
        }

        String data = dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();

        String generatedSignature = hmacSHA256(data, apiSecret);

        if (!generatedSignature.equals(dto.getRazorpaySignature())) {
            throw new RuntimeException("Invalid payment signature");
        }

        // 🔥 AFTER verification SUCCESS → update DB here
        Payment payment = paymentRepository.findByRazorpayOrderId(dto.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setRazorpayPaymentId(dto.getRazorpayPaymentId());
        payment.setRazorpaySignature(dto.getRazorpaySignature());
        payment.setPaymentStatus("success");

        paymentRepository.save(payment);
        quoteService.updateQuoteStatus(dto.getQuoteId(), "bound");
    }

    private String hmacSHA256(String data, String secret) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKeySpec =
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    @Transactional
    public PaymentReceiptDto getPaymentDetails(Long quoteId, String brokerEmail){

        Broker broker = brokerRepository.findByEmail(brokerEmail);
        if(broker == null){
            throw new RuntimeException("Broker not found");
        }

        Payment payment = paymentRepository.findByQuoteQuoteIdAndPaymentStatus(quoteId, "success")
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        return new PaymentReceiptDto(
                payment.getRazorpayPaymentId(),
                payment.getRazorpayOrderId(),
                payment.getReceiptId(),
                payment.getAmount(),
                payment.getPaymentStatus(),
                payment.getQuote().getQuoteId(),
                payment.getPaymentDate()
        );
    }
}
