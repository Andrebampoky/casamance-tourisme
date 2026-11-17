package sn.casamance.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {
  private final BookingRepository bookings = mock(BookingRepository.class);
  private final VisitRepository visits = mock(VisitRepository.class);
  private final TicketService tickets = mock(TicketService.class);
  private final TicketRepository ticketRepository = mock(TicketRepository.class);
  private final MailOutboxRepository mailOutbox = mock(MailOutboxRepository.class);
  private final BookingService service = new BookingService(bookings, visits, tickets, ticketRepository, mailOutbox);
  private final LocalDate date = LocalDate.now().plusDays(3);

  @Test void computesPriceAndReturnsOpaqueReference() {
    Visit visit = visit();
    when(visits.findByIdForReservation(1L)).thenReturn(Optional.of(visit));
    when(bookings.reservedTravelers(1L, date)).thenReturn(0L);
    when(bookings.save(any())).thenAnswer(call -> call.getArgument(0));

    BookingResponse response = service.create(new BookingRequest(1L, "Awa", "awa@example.sn", date, 2));

    assertEquals(new BigDecimal("24000"), response.total());
    assertEquals("PENDING", response.status());
    assertNotNull(response.reference());
    assertEquals(36, response.reference().length());
    verify(bookings, never()).findAll();
  }

  @Test void rejectsBookingAboveDailyCapacity() {
    when(visits.findByIdForReservation(1L)).thenReturn(Optional.of(visit()));
    when(bookings.reservedTravelers(1L, date)).thenReturn(11L);

    ResponseStatusException error = assertThrows(ResponseStatusException.class,
        () -> service.create(new BookingRequest(1L, "Awa", "awa@example.sn", date, 2)));

    assertEquals(409, error.getStatusCode().value());
    verify(bookings, never()).save(any());
  }

  @Test void hidesContactDetailsFromBookingResponse() {
    Visit visit = visit();
    Booking booking = new Booking(visit, "Awa", "awa@example.sn", date, 1);
    BookingResponse response = BookingResponse.from(booking);
    assertEquals("Pirogue", response.visitTitle());
    assertFalse(response.toString().contains("awa@example.sn"));
    assertFalse(response.toString().contains("Awa"));
  }

  @Test void ticketCanOnlyBeIssuedForConfirmedReservation() {
    Booking booking = new Booking(visit(), "Awa", "awa@example.sn", date, 1);
    assertThrows(IllegalArgumentException.class, () -> new Ticket(booking));
    booking.confirm();
    assertEquals("VALID", new Ticket(booking).getStatus());
    assertThrows(IllegalStateException.class, booking::confirm);
  }

  @Test void cancelledReservationCannotBeConfirmedAndCannotUseCapacity() {
    Booking booking = new Booking(visit(), "Awa", "awa@example.sn", date, 1);
    booking.cancel(LocalDate.now());
    assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    assertThrows(IllegalStateException.class, booking::confirm);
  }

  @Test void completedReservationCannotBeCancelled() {
    Booking booking = new Booking(visit(), "Awa", "awa@example.sn", date, 1);
    ReflectionTestUtils.setField(booking, "visitDate", LocalDate.now().minusDays(2));
    booking.confirm();
    assertTrue(booking.completeIfPast(LocalDate.now()));
    assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    assertThrows(IllegalStateException.class, () -> booking.cancel(LocalDate.now()));
  }

  @Test void confirmationIssuesOneTicketAndQueuesOneEmail() {
    Booking booking = new Booking(visit(), "Awa", "awa@example.sn", date, 1);
    when(bookings.findByReferenceForUpdate("ref-1")).thenReturn(Optional.of(booking));
    when(bookings.save(any())).thenAnswer(call -> call.getArgument(0));
    when(mailOutbox.save(any())).thenAnswer(call -> call.getArgument(0));

    BookingResponse response = service.confirm("ref-1");

    assertEquals("CONFIRMED", response.status());
    verify(tickets).issue(booking);
    verify(mailOutbox).save(any(MailOutbox.class));
  }

  private Visit visit() {
    Visit visit = new Visit("Pirogue", "Balade", "Ziguinchor", "Nature", "1h30", new BigDecimal("12000"), "river", 4.8, 12);
    ReflectionTestUtils.setField(visit, "id", 1L);
    return visit;
  }
}
