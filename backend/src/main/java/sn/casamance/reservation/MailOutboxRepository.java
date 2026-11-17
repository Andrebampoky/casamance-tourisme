package sn.casamance.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

interface MailOutboxRepository extends JpaRepository<MailOutbox, Long> {
  java.util.Optional<MailOutbox> findByBookingReference(String bookingReference);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<MailOutbox> findTop10ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(String status, LocalDateTime time);
}
