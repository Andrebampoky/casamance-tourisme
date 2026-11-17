package sn.casamance.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

record InterestOption(String code, String label, String description, String icon) {}

record ItineraryResponse(String reference, LocalDate startDate, LocalDate endDate, int durationDays,
    int travelers, List<String> interests, BigDecimal budget, BigDecimal estimatedTotal,
    BigDecimal budgetRemaining, String currency, List<ItineraryStop> stops) {
  static ItineraryResponse from(Itinerary itinerary) {
    List<String> interests = Arrays.stream(itinerary.getInterests().split(",")).filter(code -> !code.isBlank()).toList();
    return new ItineraryResponse(itinerary.getReference(), itinerary.getStartDate(),
        itinerary.getStartDate().plusDays(itinerary.getDurationDays() - 1L), itinerary.getDurationDays(),
        itinerary.getTravelers(), interests, itinerary.getBudget(), itinerary.getEstimatedTotal(),
        itinerary.getBudget().subtract(itinerary.getEstimatedTotal()), "XOF", itinerary.getStops());
  }
}
