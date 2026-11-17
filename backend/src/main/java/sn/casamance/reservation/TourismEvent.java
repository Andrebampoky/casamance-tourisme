package sn.casamance.reservation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
class TourismEvent {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String title;
  @Column(nullable=false, length=2000) private String description;
  @Column(nullable=false) private String type;
  @Column(nullable=false) private String location;
  @Column(nullable=false) private LocalDateTime startsAt;
  @Column(nullable=false) private LocalDateTime endsAt;
  @Column(length=1000) private String practicalInfo;
  @Column(nullable=false, length=500) private String imageUrl;

  protected TourismEvent() {}
  TourismEvent(String title, String description, String type, String location,
      LocalDateTime startsAt, LocalDateTime endsAt, String practicalInfo, String imageUrl) {
    this.title = title.trim();
    this.description = description.trim();
    this.type = type.trim();
    this.location = location.trim();
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.practicalInfo = practicalInfo == null ? "" : practicalInfo.trim();
    this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
  }
  Long getId() { return id; }
  String getTitle() { return title; }
  String getDescription() { return description; }
  String getType() { return type; }
  String getLocation() { return location; }
  LocalDateTime getStartsAt() { return startsAt; }
  LocalDateTime getEndsAt() { return endsAt; }
  String getPracticalInfo() { return practicalInfo; }
  String getImageUrl() { return imageUrl; }
}
