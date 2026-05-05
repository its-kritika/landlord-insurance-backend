package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.entity.ResetPwdOtp;
import com.capstone.landlordInsurance.service.BrokerService;
import com.capstone.landlordInsurance.service.EmailService;
import com.capstone.landlordInsurance.service.ResetPwdOtpService;
import com.capstone.landlordInsurance.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/broker")
public class OtpController {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ResetPwdOtpService resetPwdOtpService;

    @PostMapping("sendOtp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        try{
            String brokerEmail = request.get("email");
            Broker findBroker = brokerService.findByEmail(brokerEmail);
            if (findBroker == null){
                throw new RuntimeException("Email not registered!");
            }

            String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
            resetPwdOtpService.saveOtp(brokerEmail, otp);

            emailService.sendEmail(
                    brokerEmail,
                    "Reset Your Password",
                    "Your OTP for password reset is: " + otp + "\nThis OTP is valid for 10 minutes and can only be used once."
            );

            response.put("message", "OTP sent successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("error", "Failed to send OTP. Please try again!");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        try{
            String brokerEmail = request.get("email");
            String enteredOtp = request.get("otp");
            ResetPwdOtp otpRecord = resetPwdOtpService.getOtp(brokerEmail);

            if (!otpRecord.getOtp().equals(enteredOtp)) {
                throw new RuntimeException("Invalid OTP!");
            }

            if (otpRecord.getExpiryTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("OTP has expired!");
            }

            if (otpRecord.isOtpUsedOnce()) {
                throw new RuntimeException("OTP has already been used!");
            }

            otpRecord.setOtpUsedOnce(true);
            resetPwdOtpService.markOtpAsUsed(otpRecord);

            String jwtToken = jwtUtils.generateToken(brokerEmail);

            response.put("message", "OTP verified successfully!");
            response.put("resetToken", jwtToken);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("error", "Failed to send OTP. Please try again!");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
