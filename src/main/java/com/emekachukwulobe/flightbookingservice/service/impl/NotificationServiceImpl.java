package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.Passenger;
import com.emekachukwulobe.flightbookingservice.domain.Ticket;
import com.emekachukwulobe.flightbookingservice.repository.TicketRepository;
import com.emekachukwulobe.flightbookingservice.service.EmailTemplateBuilder;
import com.emekachukwulobe.flightbookingservice.service.NotificationService;
import com.emekachukwulobe.flightbookingservice.service.TenantConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Asynchronous notification service. Sends emails and SMS to passengers.
 * All failures are caught and logged — they must never propagate to the booking transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final TenantConfigService tenantConfigService;
    private final EmailTemplateBuilder emailTemplateBuilder;
    private final TwilioSmsService twilioSmsService;
    private final TicketRepository ticketRepository;

    @Async
    @Override
    public void sendBookingConfirmation(Booking booking) {
        UUID tenantId = booking.getTenantId();
        boolean emailEnabled = tenantConfigService.isEmailEnabled(tenantId);
        boolean smsEnabled   = tenantConfigService.isSmsEnabled(tenantId);

        if (!emailEnabled && !smsEnabled) return;

        var fromName  = tenantConfigService.getConfigValue(tenantId, "SMTP_FROM_NAME", "Flight Booking");
        var fromEmail = tenantConfigService.getConfigValue(tenantId, "SMTP_FROM_EMAIL", null);

        for (Passenger passenger : booking.getPassengers()) {
            if (emailEnabled && passenger.getEmail() != null) {
                try {
                    String html = emailTemplateBuilder.buildBookingConfirmation(booking, passenger);
                    sendEmail(tenantId, passenger.getEmail(),
                        "Booking Confirmation — " + booking.getBookingReference(),
                        html, fromEmail, fromName);
                } catch (Exception e) {
                    log.error("Failed to send booking confirmation email to {}: {}", passenger.getEmail(), e.getMessage());
                }
            }

            if (smsEnabled && passenger.getPhoneNumber() != null) {
                try {
                    String sms = "Your booking %s is confirmed. Flight %s from %s to %s departing %s."
                        .formatted(
                            booking.getBookingReference(),
                            booking.getFlight().getFlightNumber(),
                            booking.getFlight().getOrigin(),
                            booking.getFlight().getDestination(),
                            booking.getFlight().getDepartureTime().toString());
                    twilioSmsService.sendSms(tenantId, passenger.getPhoneNumber(), sms);
                } catch (Exception e) {
                    log.error("Failed to send booking SMS to {}: {}", passenger.getPhoneNumber(), e.getMessage());
                }
            }
        }
    }

    @Async
    @Override
    public void sendTicketDetails(Booking booking) {
        UUID tenantId = booking.getTenantId();
        boolean emailEnabled = tenantConfigService.isEmailEnabled(tenantId);
        boolean smsEnabled   = tenantConfigService.isSmsEnabled(tenantId);

        if (!emailEnabled && !smsEnabled) return;

        List<Ticket> tickets = ticketRepository.findAllByBookingId(booking.getId());
        var fromName  = tenantConfigService.getConfigValue(tenantId, "SMTP_FROM_NAME", "Flight Booking");
        var fromEmail = tenantConfigService.getConfigValue(tenantId, "SMTP_FROM_EMAIL", null);

        // Map passenger id -> ticket
        Map<UUID, Ticket> ticketMap = new java.util.HashMap<>();
        tickets.forEach(t -> ticketMap.put(t.getPassenger().getId(), t));

        for (Passenger passenger : booking.getPassengers()) {
            Ticket ticket = ticketMap.get(passenger.getId());
            if (ticket == null) continue;

            if (emailEnabled && passenger.getEmail() != null) {
                try {
                    String html = emailTemplateBuilder.buildTicketDetails(booking, passenger, ticket);
                    sendEmail(tenantId, passenger.getEmail(),
                        "Your Ticket — " + ticket.getTicketNumber(),
                        html, fromEmail, fromName);
                } catch (Exception e) {
                    log.error("Failed to send ticket email to {}: {}", passenger.getEmail(), e.getMessage());
                }
            }

            if (smsEnabled && passenger.getPhoneNumber() != null) {
                try {
                    String sms = composeSmsMessage(booking, ticket);
                    twilioSmsService.sendSms(tenantId, passenger.getPhoneNumber(), sms);
                } catch (Exception e) {
                    log.error("Failed to send ticket SMS to {}: {}", passenger.getPhoneNumber(), e.getMessage());
                }
            }
        }
    }

    private static String composeSmsMessage(Booking booking, Ticket ticket) {
        String seatInfo = ticket.getSeatNumber() != null ? " | Seat: " + ticket.getSeatNumber() : "";
        return "Ticket: %s%s | Ref: %s | %s %s→%s %s. Present at airport."
            .formatted(
                ticket.getTicketNumber(), seatInfo,
                booking.getBookingReference(),
                booking.getFlight().getFlightNumber(),
                booking.getFlight().getOrigin(),
                booking.getFlight().getDestination(),
                booking.getFlight().getDepartureTime().toString());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void sendEmail(UUID tenantId, String to, String subject, String html,
                           String fromEmail, String fromName) throws Exception {
        JavaMailSenderImpl sender = buildMailSender(tenantId);
        if (sender == null || fromEmail == null) {
            log.warn("Email not configured for tenant {}. Skipping email to {}", tenantId, to);
            return;
        }

        var message = sender.createMimeMessage();
        var helper  = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        helper.setFrom(fromEmail, fromName);
        sender.send(message);
        log.info("Email sent to {} for tenant {}", to, tenantId);
    }

    private JavaMailSenderImpl buildMailSender(UUID tenantId) {
        String host     = tenantConfigService.getConfigValue(tenantId, "SMTP_HOST", null);
        String port     = tenantConfigService.getConfigValue(tenantId, "SMTP_PORT", "587");
        String username = tenantConfigService.getConfigValue(tenantId, "SMTP_USERNAME", null);
        String password = tenantConfigService.getConfigValue(tenantId, "SMTP_PASSWORD", null);

        if (host == null || username == null || password == null) return null;

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(Integer.parseInt(port));
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return sender;
    }
}
