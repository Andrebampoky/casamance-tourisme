package sn.casamance.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface TicketRepository extends JpaRepository<Ticket, Long> {
  Optional<Ticket> findByBooking_Reference(String reference);
  Optional<Ticket> findByToken(String token);
}
