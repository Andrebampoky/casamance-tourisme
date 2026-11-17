package sn.casamance.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Public reservation view. Personal contact details are never serialized. */
record BookingResponse(
    Long id,
    String reference,
    String visitTitle,
    String location,
    String image,
    Long visitId,
    LocalDate visitDate,
    int travelers,
    BigDecimal total,
    String status) {
  static BookingResponse from(Booking booking) {
    Visit visit = booking.getVisit();
    return new BookingResponse(booking.getId(), booking.getReference(), visit.getTitle(), visit.getLocation(),
        visit.getImage(), visit.getId(), booking.getVisitDate(), booking.getTravelers(), booking.getTotal(), booking.getStatus().name());
  }
}
