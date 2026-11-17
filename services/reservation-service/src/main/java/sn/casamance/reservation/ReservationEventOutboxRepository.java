package sn.casamance.reservation;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDateTime; import java.util.*;
interface ReservationEventOutboxRepository extends JpaRepository<ReservationEventOutbox,Long>{ Optional<ReservationEventOutbox> findByBookingReferenceAndEventType(String ref,String type); List<ReservationEventOutbox> findTop20ByStatusAndNextAttemptAtBeforeOrderByIdAsc(String status,LocalDateTime now); }
