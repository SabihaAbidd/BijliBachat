package com.example.bijlibachat.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailObserver implements ConsumptionObserver {

    private static final String SENDER_EMAIL = "sukainazainab1214@gmail.com"; // your gmail here
    private static final String APP_PASSWORD  = "tgdhmblbjmlotipv";   // your app password here (no spaces)

    private String userEmail;

    public EmailObserver(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public void onThresholdReached(float consumption, float threshold, float costRisk) {
        String subject = "Bijli Bachat — Usage Warning";
        String body =
                "Dear Bijli Bachat User,\n\n" +
                        "This is an automated alert from Bijli Bachat.\n\n" +
                        "You have used " + (int)consumption + " units out of your " +
                        (int)threshold + " unit ceiling.\n" +
                        "That is over 80% of your monthly limit.\n\n" +
                        "Estimated worst-case cost risk: Rs. " + (int)costRisk + "\n\n" +
                        "Please reduce your electricity usage to avoid jumping into a higher tariff tier.\n\n" +
                        "Regards,\nBijli Bachat System";
        sendEmail(subject, body);
    }

    @Override
    public void onCeilingExceeded(float consumption, float ceiling) {
        String subject = "Bijli Bachat — Ceiling Exceeded!";
        String body =
                "Dear Bijli Bachat User,\n\n" +
                        "This is an urgent alert from Bijli Bachat.\n\n" +
                        "You have EXCEEDED your monthly ceiling!\n" +
                        "Units consumed: " + (int)consumption + "\n" +
                        "Your ceiling: " + (int)ceiling + "\n\n" +
                        "You are now being charged at the highest tariff rate (Rs. 19.55/unit).\n\n" +
                        "Please take immediate action to reduce your electricity usage.\n\n" +
                        "Regards,\nBijli Bachat System";
        sendEmail(subject, body);
    }

    private void sendEmail(String subject, String body) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                System.out.println("Email sent successfully to: " + userEmail);

            } catch (Exception e) {
                System.out.println("Email failed: " + e.getMessage());
            }
        }).start();
    }
}