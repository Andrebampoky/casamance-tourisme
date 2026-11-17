package sn.casamance.reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
interface VisitRepository extends JpaRepository<Visit,Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select v from Visit v where v.id = :id")
  Optional<Visit> findByIdForReservation(@Param("id") Long id);
}
