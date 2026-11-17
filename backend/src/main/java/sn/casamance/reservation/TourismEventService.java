package sn.casamance.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
class TourismEventService {
  private final TourismEventRepository events;
  TourismEventService(TourismEventRepository events) { this.events = events; }

  @Transactional(readOnly = true)
  List<TourismEventResponse> search(String type, LocalDate date, LocalDate from, LocalDate to, String location, String q) {
    if (from != null && to != null && from.isAfter(to)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début doit précéder la date de fin");
    LocalDate effectiveFrom = date != null && (from == null || date.isAfter(from)) ? date : from;
    LocalDate effectiveTo = date != null && (to == null || date.isBefore(to)) ? date : to;
    if (effectiveFrom != null && effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) return List.of();
    LocalDateTime lower = effectiveFrom == null ? null : effectiveFrom.atStartOfDay();
    LocalDateTime upper = effectiveTo == null ? null : effectiveTo.plusDays(1).atStartOfDay();
    String keyword = normalize(q);
    String place = normalize(location);
    String selectedType = normalize(type);
    return events.findAll().stream()
        .filter(event -> selectedType.isBlank() || normalize(event.getType()).equals(selectedType))
        .filter(event -> place.isBlank() || normalize(event.getLocation()).contains(place))
        .filter(event -> lower == null || !event.getEndsAt().isBefore(lower))
        .filter(event -> upper == null || event.getStartsAt().isBefore(upper))
        .filter(event -> keyword.isBlank() || normalize(event.getTitle() + " " + event.getDescription() + " " + event.getType() + " " + event.getLocation()).contains(keyword))
        .sorted((left, right) -> left.getStartsAt().compareTo(right.getStartsAt()))
        .map(TourismEventResponse::from).toList();
  }

  @Transactional
  TourismEventResponse create(TourismEventRequest request) {
    if (!request.endsAt().isAfter(request.startsAt())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fin doit être après le début");
    return TourismEventResponse.from(events.save(new TourismEvent(request.title(), request.description(), request.type(),
        request.location(), request.startsAt(), request.endsAt(), request.practicalInfo(), request.imageUrl())));
  }

  private String normalize(String value) {
    if (value == null) return "";
    return Normalizer.normalize(value.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
  }
}
