package com.ciphermarket.api.disclosure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class DisclosureNotificationService {

    private static final Logger log = LoggerFactory.getLogger(DisclosureNotificationService.class);

    private final JavaMailSender mailSender;

    public DisclosureNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDisclosureInvite(String recipientEmail, String documentTitle, String organisationHint) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("CipherMarket confidential disclosure invitation");
        message.setText("""
                You have been invited to review a confidential document on CipherMarket.

                Document: %s
                Organisation: %s

                Sign in and open your disclosures inbox to read the terms and accept or reject access.
                Hashing and acceptance create an evidence trail of disclosure — they do not automatically
                create copyright, patent protection, or a legally binding NDA.
                """.formatted(documentTitle, organisationHint));
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Disclosure invite mail failed for {}: {}", recipientEmail, e.getMessage());
        }
    }
}
