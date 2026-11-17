package sn.casamance.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;

record TicketResponse(String reservationReference, String activity, String location, LocalDate visitDate,
    int participants, BigDecimal total, String status, String qrSvgDataUri) {}

record TicketValidation(boolean valid, String status, String activity, LocalDate visitDate) {}
