package sn.casamance.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/** Application use cases for creating and retrieving reservations. */
@Service
class BookingService {
  private final BookingRepository bookings;
  private final VisitRepository visits;
  private final TicketService tickets;
  private final TicketRepository ticketRepository;
  private final ReservationEventOutboxRepository integrationEvents;

  BookingService(BookingRepository bookings, VisitRepository visits, TicketService tickets,
      TicketRepository ticketRepository, ReservationEventOutboxRepository integrationEvents) {
    this.bookings = bookings;
    this.visits = visits;
    this.tickets = tickets;
    this.ticketRepository = ticketRepository;
    this.integrationEvents = integrationEvents;
  }

  @Transactional
  BookingResponse create(BookingRequest request) {
    // Serialize capacity checks for a visit so parallel requests cannot both claim its last seats.
    Visit visit = visits.findByIdForReservation(request.visitId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visite inconnue"));
    if (bookings.reservedTravelers(visit.getId(), request.visitDate()) + request.travelers() > visit.getCapacity()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Capacité indisponible pour cette date");
    }
    return BookingResponse.from(bookings.save(new Booking(visit, request.customerName(), request.email(), request.visitDate(), request.travelers())));
  }

  @Transactional
  BookingResponse findByReference(String reference) {
    Booking booking = bookings.findByReference(reference)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation inconnue"));
    booking.completeIfPast(java.time.LocalDate.now());
    return BookingResponse.from(booking);
  }

  @Transactional
  BookingResponse confirm(String reference) {
    Booking booking = bookings.findByReferenceForUpdate(reference)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation inconnue"));
    try {
      booking.confirm();
    } catch (IllegalStateException error) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, error.getMessage());
    }
    tickets.issue(booking);
    integrationEvents.save(ReservationEventOutbox.confirmed(booking));
    return BookingResponse.from(booking);
  }

  @Transactional
  BookingResponse cancel(String reference) {
    Booking booking = bookings.findByReferenceForUpdate(reference)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation inconnue"));
    try {
      booking.cancel(java.time.LocalDate.now());
    } catch (IllegalStateException error) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, error.getMessage());
    }
    ticketRepository.findByBooking_Reference(reference).ifPresent(Ticket::revoke);
    integrationEvents.findByBookingReferenceAndEventType(reference, "BOOKING_CONFIRMED")
        .filter(ReservationEventOutbox::isPending).ifPresent(ReservationEventOutbox::cancelIfPending);
    integrationEvents.findByBookingReferenceAndEventType(reference, "BOOKING_CANCELLED")
        .orElseGet(() -> integrationEvents.save(ReservationEventOutbox.cancelled(booking)));
    return BookingResponse.from(booking);
  }

  @Transactional(readOnly = true)
  List<BookingResponse> listForAdmin(BookingStatus status) {
    return bookings.findByStatusOrderByCreatedAtDesc(status).stream().map(BookingResponse::from).toList();
  }
}
