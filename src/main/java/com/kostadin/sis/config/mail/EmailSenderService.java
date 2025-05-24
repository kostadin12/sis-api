package com.kostadin.sis.config.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService {

    private final JavaMailSender mailSender;

    public void sendEmail(EmailRequestBody emailRequestBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailRequestBody.getSentFrom());
            helper.setTo(emailRequestBody.getSendTo().split(", "));
            helper.setSubject(emailRequestBody.getSubject());
            helper.setText(emailRequestBody.getBody(), "text/html".equals(emailRequestBody.getBodyContentType()));

            if (emailRequestBody.getReplyTo() != null && !emailRequestBody.getReplyTo().isEmpty()) {
                helper.setReplyTo(emailRequestBody.getReplyTo());
            }

            mailSender.send(message);
            log.info("Email sent successfully to: {}", emailRequestBody.getSendTo());
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }
}