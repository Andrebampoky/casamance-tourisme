package sn.casamance.reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
public interface BookingRepository extends JpaRepository<Booking,Long> {
 @Query("select coalesce(sum(b.travelers),0) from Booking b where b.visit.id=:visitId and b.visitDate=:visitDate and b.status <> sn.casamance.reservation.BookingStatus.CANCELLED")
 long reservedTravelers(@Param("visitId") Long visitId,@Param("visitDate") LocalDate visitDate);
 Optional<Booking> findByReference(String reference);
 java.util.List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select b from Booking b where b.reference=:reference")
 Optional<Booking> findByReferenceForUpdate(@Param("reference") String reference);
}
