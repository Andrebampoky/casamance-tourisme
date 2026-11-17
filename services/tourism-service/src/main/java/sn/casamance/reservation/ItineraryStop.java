package sn.casamance.reservation;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class ItineraryStop {
  private int dayNumber;
  private int stopOrder;
  private Long visitId;
  private String title;
  private String location;
  private String duration;
  private BigDecimal estimatedCost;

  protected ItineraryStop() {}
  ItineraryStop(int dayNumber, int stopOrder, VisitSnapshot visit, BigDecimal estimatedCost) {
    this.dayNumber = dayNumber;
    this.stopOrder = stopOrder;
    this.visitId = visit.id();
    this.title = visit.title();
    this.location = visit.location();
    this.duration = visit.duration();
    this.estimatedCost = estimatedCost;
  }
  public int getDayNumber() { return dayNumber; }
  public int getStopOrder() { return stopOrder; }
  public Long getVisitId() { return visitId; }
  public String getTitle() { return title; }
  public String getLocation() { return location; }
  public String getDuration() { return duration; }
  public BigDecimal getEstimatedCost() { return estimatedCost; }
}
