package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.Passenger;
import com.emekachukwulobe.flightbookingservice.domain.Ticket;
import com.emekachukwulobe.flightbookingservice.service.EmailTemplateBuilder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateBuilderImpl implements EmailTemplateBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm z");

    @Override
    public String buildBookingConfirmation(Booking booking, Passenger passenger) {
        var flight = booking.getFlight();
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Booking Confirmation</title></head>
            <body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px">
              <div style="max-width:600px;margin:auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)">
                <div style="background:#1a56db;color:#fff;padding:24px 32px">
                  <h1 style="margin:0;font-size:22px">Booking Confirmed ✈</h1>
                  <p style="margin:4px 0 0;opacity:.85">Your booking is pending payment confirmation</p>
                </div>
                <div style="padding:32px">
                  <p>Dear <strong>%s %s</strong>,</p>
                  <p>Your booking has been received. Here are your details:</p>
                  <table style="width:100%%;border-collapse:collapse;margin:16px 0">
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Booking Reference</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Flight</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>From</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>To</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Departure</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Arrival</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                  </table>
                  <p style="color:#6b7280;font-size:13px">Please complete payment to confirm your seat. Your booking will expire if payment is not received.</p>
                </div>
                <div style="background:#f3f4f6;padding:16px 32px;font-size:12px;color:#6b7280">
                  This is an automated email. Please do not reply.
                </div>
              </div>
            </body></html>
            """.formatted(
                passenger.getFirstName(), passenger.getLastName(),
                booking.getBookingReference(),
                flight.getFlightNumber(),
                flight.getOrigin(), flight.getDestination(),
                flight.getDepartureTime().format(DATE_FMT),
                flight.getArrivalTime().format(DATE_FMT)
        );
    }

    @Override
    public String buildTicketDetails(Booking booking, Passenger passenger, Ticket ticket) {
        var flight = booking.getFlight();
        String seatInfo = ticket.getSeatNumber() != null ? ticket.getSeatNumber() : "To be assigned at check-in";
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Your Ticket</title></head>
            <body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px">
              <div style="max-width:600px;margin:auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)">
                <div style="background:#059669;color:#fff;padding:24px 32px">
                  <h1 style="margin:0;font-size:22px">Your Boarding Pass 🎫</h1>
                  <p style="margin:4px 0 0;opacity:.85">Please present this at the airport</p>
                </div>
                <div style="padding:32px">
                  <p>Dear <strong>%s %s</strong>,</p>
                  <p>Your booking is confirmed! Here is your ticket information:</p>
                  <table style="width:100%%;border-collapse:collapse;margin:16px 0">
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Ticket Number</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb;font-family:monospace;font-size:16px">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Booking Reference</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb;font-family:monospace;font-size:16px">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Seat</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb;font-size:20px;font-weight:bold;color:#1a56db">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Flight</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>From → To</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s → %s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Departure</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Arrival</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                    <tr><td style="padding:8px;background:#f9fafb;border:1px solid #e5e7eb"><strong>Passenger</strong></td>
                        <td style="padding:8px;border:1px solid #e5e7eb">%s %s</td></tr>
                  </table>
                  <p style="background:#fef3c7;border-left:4px solid #f59e0b;padding:12px;border-radius:4px;font-size:13px">
                    ⚠ Please arrive at least 2 hours before departure and present this ticket at the check-in counter.
                  </p>
                </div>
                <div style="background:#f3f4f6;padding:16px 32px;font-size:12px;color:#6b7280">
                  This is an automated email. Please do not reply.
                </div>
              </div>
            </body></html>
            """.formatted(
                passenger.getFirstName(), passenger.getLastName(),
                ticket.getTicketNumber(),
                booking.getBookingReference(),
                seatInfo,
                flight.getFlightNumber(),
                flight.getOrigin(), flight.getDestination(),
                flight.getDepartureTime().format(DATE_FMT),
                flight.getArrivalTime().format(DATE_FMT),
                passenger.getFirstName(), passenger.getLastName()
        );
    }
}
