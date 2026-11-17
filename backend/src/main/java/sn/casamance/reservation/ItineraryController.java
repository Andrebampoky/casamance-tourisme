package sn.casamance.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
class ItineraryController {
  private final ItineraryService itineraries;
  ItineraryController(ItineraryService itineraries) { this.itineraries = itineraries; }

  @GetMapping("/interests")
  List<InterestOption> interests() { return itineraries.interests(); }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ItineraryResponse generate(@Valid @RequestBody ItineraryRequest request) { return itineraries.generate(request); }

  @GetMapping("/{reference}")
  ItineraryResponse get(@PathVariable String reference) { return itineraries.get(reference); }
}
