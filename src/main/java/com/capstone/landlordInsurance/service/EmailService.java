package com.capstone.landlordInsurance.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body){
        try{
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            javaMailSender.send(mail);

        } catch (Exception e) {
            log.error("Exception while send email!", e);
        }
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachmentBytes,
            String fileName) {

        try {

            MimeMessage message =
                    javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            helper.addAttachment(
                    fileName,
                    new ByteArrayResource(
                            attachmentBytes
                    )
            );

            javaMailSender.send(message);

        } catch (Exception e) {
            log.error(
                    "Exception while sending email with attachment!",
                    e
            );
        }
    }
}
