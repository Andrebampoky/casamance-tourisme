package sn.casamance.reservation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
class Itinerary {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=36) private String reference = UUID.randomUUID().toString();
  @Column(nullable=false) private LocalDate startDate;
  @Column(nullable=false) private int durationDays;
  @Column(nullable=false) private int travelers;
  @Column(nullable=false, precision=12, scale=2) private BigDecimal budget;
  @Column(nullable=false, precision=12, scale=2) private BigDecimal estimatedTotal;
  @Column(nullable=false, length=400) private String interests;
  @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
  @ElementCollection(fetch=FetchType.EAGER)
  @CollectionTable(name="itinerary_stops", joinColumns=@JoinColumn(name="itinerary_id"))
  @OrderColumn(name="stop_index")
  private List<ItineraryStop> stops = new ArrayList<>();

  protected Itinerary() {}
  Itinerary(LocalDate startDate, int durationDays, int travelers, BigDecimal budget,
      List<String> interests, BigDecimal estimatedTotal, List<ItineraryStop> stops) {
    this.startDate = startDate;
    this.durationDays = durationDays;
    this.travelers = travelers;
    this.budget = budget;
    this.interests = String.join(",", interests);
    this.estimatedTotal = estimatedTotal;
    this.stops = new ArrayList<>(stops);
  }
  Long getId() { return id; }
  String getReference() { return reference; }
  LocalDate getStartDate() { return startDate; }
  int getDurationDays() { return durationDays; }
  int getTravelers() { return travelers; }
  BigDecimal getBudget() { return budget; }
  BigDecimal getEstimatedTotal() { return estimatedTotal; }
  String getInterests() { return interests; }
  List<ItineraryStop> getStops() { return List.copyOf(stops); }
}
