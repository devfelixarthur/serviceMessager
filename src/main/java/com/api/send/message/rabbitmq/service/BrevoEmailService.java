package com.api.send.message.rabbitmq.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class BrevoEmailService {
    private final JavaMailSender javaMailSender;

    public BrevoEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void enviarEmail(String to, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Recuperação de Senha - Futebol Hub");
        helper.setFrom("arthurflpadula@gmail.com");
        helper.setText(htmlContent, true);
        javaMailSender.send(mimeMessage);
    }
}
