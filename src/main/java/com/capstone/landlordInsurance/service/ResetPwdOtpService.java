package com.capstone.landlordInsurance.service;

import com.capstone.landlordInsurance.entity.Client;
import com.capstone.landlordInsurance.entity.ResetPwdOtp;
import com.capstone.landlordInsurance.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResetPwdOtpService {

    @Autowired
    private OtpRepository OtpRepository;

    public void saveOtp(String brokerEmail, String otp) {
        ResetPwdOtp resetPwdOtp = new ResetPwdOtp();
        resetPwdOtp.setBrokerEmail(brokerEmail);
        resetPwdOtp.setOtp(otp);
        resetPwdOtp.setOtpUsedOnce(false);
        resetPwdOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));

        OtpRepository.save(resetPwdOtp);

    }

    public ResetPwdOtp getOtp(String brokerEmail) {
        return OtpRepository
                .findTopByBrokerEmailOrderByCreatedAtDesc(brokerEmail)
                .orElse(null);
    }

    public void markOtpAsUsed(ResetPwdOtp otpRecord) {
        otpRecord.setOtpUsedOnce(true);
        OtpRepository.save(otpRecord);
    }
}
