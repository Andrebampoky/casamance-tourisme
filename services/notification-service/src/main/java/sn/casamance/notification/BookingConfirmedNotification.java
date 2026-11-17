package sn.casamance.notification;
import java.math.BigDecimal;
import java.time.LocalDate;
public record BookingConfirmedNotification(String bookingReference, String recipient, String customerName, String activity, String location, LocalDate visitDate, int participants, BigDecimal total) {}
