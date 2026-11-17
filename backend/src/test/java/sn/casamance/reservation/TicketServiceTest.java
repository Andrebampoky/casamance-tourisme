package sn.casamance.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {
  private final TicketRepository tickets = mock(TicketRepository.class);
  private final TicketService service = new TicketService(tickets, "https://tourisme.example.sn");

  @Test void producesScannableQrSvgForAConfirmedBooking() {
    Visit visit = new Visit("Balade en pirogue", "Découverte du fleuve", "Ziguinchor", "Nature", "1h 30",
        new BigDecimal("12000"), "river", 4.9, 12);
    ReflectionTestUtils.setField(visit, "id", 2L);
    Booking booking = new Booking(visit, "Awa", "awa@example.sn", LocalDate.now().plusDays(2), 2);
    booking.confirm();
    Ticket ticket = new Ticket(booking);
    when(tickets.findByBooking_Reference(booking.getReference())).thenReturn(Optional.of(ticket));

    TicketResponse response = service.findForReservation(booking.getReference());

    assertEquals(booking.getReference(), response.reservationReference());
    assertEquals("VALID", response.status());
    assertTrue(response.qrSvgDataUri().startsWith("data:image/svg+xml;base64,"));
    String svg = new String(Base64.getDecoder().decode(response.qrSvgDataUri().substring("data:image/svg+xml;base64,".length())));
    assertTrue(svg.startsWith("<svg"));
    assertFalse(svg.contains(booking.getEmail()));
  }

  @Test void verifiesQrTokensWithoutExposingVisitorDetails() {
    Visit visit = new Visit("Forêt de Bayotte", "Promenade", "Enampore", "Nature", "2h",
        new BigDecimal("8000"), "forest", 4.8, 10);
    Booking booking = new Booking(visit, "Awa", "awa@example.sn", LocalDate.now().plusDays(2), 1);
    booking.confirm();
    Ticket ticket = new Ticket(booking);
    when(tickets.findByToken(ticket.getToken())).thenReturn(Optional.of(ticket));

    TicketValidation result = service.verify(ticket.getToken());

    assertTrue(result.valid());
    assertEquals("Forêt de Bayotte", result.activity());
    assertFalse(result.toString().contains("awa@example.sn"));
  }
}
