package sn.casamance.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface SatisfactionSurveyRepository extends JpaRepository<SatisfactionSurvey, Long> {
  Optional<SatisfactionSurvey> findByBooking_Reference(String reference);
}
