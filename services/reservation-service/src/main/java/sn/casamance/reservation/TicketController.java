package sn.casamance.reservation;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
class TicketController {
  private final TicketService tickets;
  TicketController(TicketService tickets) { this.tickets = tickets; }

  @GetMapping("/{reservationReference}")
  TicketResponse get(@PathVariable String reservationReference) {
    return tickets.findForReservation(reservationReference);
  }

  @GetMapping("/verify/{token}")
  TicketValidation verify(@PathVariable String token) {
    return tickets.verify(token);
  }
}
