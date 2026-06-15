//package com.medicareclinic.backend.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${app.mail.from:noreply@medicareclinic.com}")
//    private String fromEmail;
//
//    @Async
//    public void sendAppointmentConfirmation(String toEmail, String patientName, String doctorUsername, LocalDateTime startTime, Long appointmentId) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(toEmail);
//            message.setSubject("Appointment Confirmation - MediCare Clinic");
//
//            String formattedTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
//            String body = String.format(
//                    "Dear %s,\n\n" +
//                    "Your appointment has been confirmed.\n\n" +
//                    "Appointment Details:\n" +
//                    "- Doctor: %s\n" +
//                    "- Date & Time: %s\n" +
//                    "- Confirmation ID: %d\n\n" +
//                    "Please arrive 10 minutes early.\n\n" +
//                    "Best regards,\n" +
//                    "MediCare Clinic",
//                    patientName, doctorUsername, formattedTime, appointmentId
//            );
//            message.setText(body);
//
//            mailSender.send(message);
//            log.info("Appointment confirmation email sent to {} for appointment {}", toEmail, appointmentId);
//        } catch (Exception e) {
//            log.error("Failed to send appointment confirmation email to {}: {}", toEmail, e.getMessage(), e);
//        }
//    }
//}
//
