package com.capstone.landlordInsurance;

import com.capstone.landlordInsurance.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendMail() {
        emailService.sendEmail("kritikapkes22@gmail.com",
                "Test mail springboot",
                "Mail sent successfully!");
    }
}
