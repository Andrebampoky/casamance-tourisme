package sn.casamance.reservation;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
class TourismEventController {
  private final TourismEventService events;
  TourismEventController(TourismEventService events) { this.events = events; }

  @GetMapping
  List<TourismEventResponse> search(@RequestParam(required=false) String type,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required=false) String location, @RequestParam(required=false) String q) {
    return events.search(type, date, from, to, location, q);
  }
}
