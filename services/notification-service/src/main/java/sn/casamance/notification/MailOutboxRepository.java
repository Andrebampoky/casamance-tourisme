package sn.casamance.notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
interface MailOutboxRepository extends JpaRepository<MailOutbox,Long>{
 Optional<MailOutbox> findByBookingReference(String reference);
 @Lock(LockModeType.PESSIMISTIC_WRITE) List<MailOutbox> findTop10ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(String status,LocalDateTime time);
}
