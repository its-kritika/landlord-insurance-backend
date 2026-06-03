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
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
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
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

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

    @Transactional
    public byte[] generatePaymentPdf(Long quoteId) {

        BaseColor primaryBlue = new BaseColor(37, 99, 235);

        Quote quote = quoteService.getQuoteById(quoteId);

        Payment payment = paymentRepository.findByQuoteQuoteIdAndPaymentStatus(quoteId, "success")
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, baos);

            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont( FontFactory.TIMES_ROMAN, 22, primaryBlue);
            Font sectionFont = FontFactory.getFont( FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont( FontFactory.HELVETICA, 11);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            // Title
            Paragraph title = new Paragraph("LANDLORD INSURANCE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            Paragraph title2 = new Paragraph("PAYMENT RECEIPT", sectionFont);
            title2.setAlignment(Element.ALIGN_CENTER);
            title2.setSpacingAfter(9f);
            document.add(title2);
//            document.add(new Paragraph(" "));

            Paragraph quoteRef = new Paragraph("Quote #" + quote.getQuoteId(), sectionFont);

            quoteRef.setAlignment(Element.ALIGN_CENTER);
            document.add(quoteRef);
            document.add(new Paragraph(" "));

            Paragraph paymentIdParagraph = new Paragraph();
            paymentIdParagraph.add(new Chunk("Payment ID: ", boldFont));
            paymentIdParagraph.add(new Chunk(payment.getRazorpayPaymentId(), normalFont));
            paymentIdParagraph.setSpacingAfter(5f);
            document.add(paymentIdParagraph);

            Paragraph paragraph2 = new Paragraph();
            paragraph2.add(new Chunk("Order ID: ", boldFont));
            paragraph2.add(new Chunk(payment.getRazorpayOrderId(), normalFont));
            paragraph2.setSpacingAfter(5f);
            document.add(paragraph2);

            Paragraph paragraph3 = new Paragraph();
            paragraph3.add(new Chunk("Amount Paid: ₹", boldFont));
            paragraph3.add(new Chunk( String.valueOf(payment.getAmount()), normalFont));
            paragraph3.setSpacingAfter(5f);
            document.add(paragraph3);

            Paragraph paragraph4 = new Paragraph();
            paragraph4.add(new Chunk("Payment Status: ", boldFont));
            paragraph4.add(new Chunk( "Success", normalFont));
            paragraph4.setSpacingAfter(5f);
            document.add(paragraph4);

            Paragraph paragraph5 = new Paragraph();
            paragraph5.add(new Chunk("Payment Date: ", boldFont));
            paragraph5.add(new Chunk( payment.getPaymentDate()
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), normalFont));
            paragraph5.setSpacingAfter(5f);
            document.add(paragraph5);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph clientHeading = new Paragraph("CLIENT INFORMATION", sectionFont);
            document.add(clientHeading);
            document.add(new Paragraph(" "));

            Paragraph paragraph6 = new Paragraph();
            paragraph6.add(new Chunk("Client Name: ", boldFont));
            paragraph6.add(new Chunk( quote.getClient().getName(), normalFont));
            paragraph6.setSpacingAfter(5f);
            document.add(paragraph6);

            Paragraph paragraph7 = new Paragraph();
            paragraph7.add(new Chunk("Address: ", boldFont));
            paragraph7.add(new Chunk( quote.getClient().getAddress(), normalFont));
            paragraph7.setSpacingAfter(5f);
            document.add(paragraph7);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph thankYou = new Paragraph("Thank you for choosing LandSure", boldFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
