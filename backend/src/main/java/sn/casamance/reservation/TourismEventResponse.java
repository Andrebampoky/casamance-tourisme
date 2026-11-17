package sn.casamance.reservation;

import java.time.LocalDateTime;

record TourismEventResponse(Long id, String title, String description, String type, String location,
    LocalDateTime startsAt, LocalDateTime endsAt, String practicalInfo, String imageUrl) {
  static TourismEventResponse from(TourismEvent event) {
    return new TourismEventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getType(),
        event.getLocation(), event.getStartsAt(), event.getEndsAt(), event.getPracticalInfo(), event.getImageUrl());
  }
}
