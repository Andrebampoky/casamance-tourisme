package sn.casamance.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
  Optional<Itinerary> findByReference(String reference);
}
