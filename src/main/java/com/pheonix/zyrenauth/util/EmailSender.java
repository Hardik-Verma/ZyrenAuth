// src/main/java/com/pheonix/zyrenauth/util/EmailSender.java
package com.pheonix.zyrenauth.util;

import com.pheonix.zyrenauth.ZyrenAuthPlugin;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailSender {

    private final ZyrenAuthConfig config;
    private Session session;

    public EmailSender(ZyrenAuthConfig config) {
        this.config = config;
        initializeMailSession();
    }

    private void initializeMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", config.getSmtpPort());
        props.put("mail.smtp.auth", String.valueOf(config.isSmtpAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isSmtpStarttlsEnable()));
        props.put("mail.smtp.ssl.trust", config.getSmtpHost());

        if (config.isSmtpAuth()) {
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
                }
            });
        } else {
            session = Session.getInstance(props);
        }
        ZyrenAuthPlugin.getInstance().getLogger()
                .info("EmailSender session initialized for host: " + config.getSmtpHost());
    }

    public boolean sendEmail(String recipientEmail, String subject, String body) {
        if (session == null) {
            ZyrenAuthPlugin.getInstance().getLogger().severe("Email session not initialized. Cannot send email.");
            return false;
        }
        try {
            MimeMessage message = new MimeMessage(session);
            String from = (config.getEmailSenderAddress() == null || config.getEmailSenderAddress().isEmpty())
                    ? config.getSmtpUsername()
                    : config.getEmailSenderAddress();

            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            ZyrenAuthPlugin.getInstance().getLogger()
                    .info("Email sent successfully to " + recipientEmail + " with subject: " + subject);
            return true;
        } catch (MessagingException e) {
            ZyrenAuthPlugin.getInstance().getLogger()
                    .severe("Failed to send email to " + recipientEmail + ": " + e.getMessage());
            if (e.getCause() instanceof AuthenticationFailedException) {
                ZyrenAuthPlugin.getInstance().getLogger()
                        .severe("Email authentication failed. Check SMTP username and password in config.json.");
            }
            return false;
        }
    }
}
