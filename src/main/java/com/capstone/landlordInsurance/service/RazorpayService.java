package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.dto.PaymentRequestDto;
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
}
