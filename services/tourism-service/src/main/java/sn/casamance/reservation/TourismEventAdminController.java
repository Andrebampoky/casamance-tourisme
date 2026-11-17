package sn.casamance.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
class TourismEventAdminController {
  private final TourismEventService events;
  private final AdminAccess adminAccess;
  TourismEventAdminController(TourismEventService events, AdminAccess adminAccess) {
    this.events = events;
    this.adminAccess = adminAccess;
  }
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  TourismEventResponse create(@RequestHeader(value="X-Admin-Token", required=false) String token,
      @Valid @RequestBody TourismEventRequest request) {
    adminAccess.require(token);
    return events.create(request);
  }
}
