package sn.casamance.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
class BookingController {
  private final BookingService bookingService;

  BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  BookingResponse create(@Valid @RequestBody BookingRequest request) {
    return bookingService.create(request);
  }

  /** The unscoped collection endpoint is intentionally absent; access requires an unguessable reference. */
  @GetMapping("/{reference}")
  BookingResponse get(@PathVariable String reference) {
    return bookingService.findByReference(reference);
  }

  @PostMapping("/{reference}/cancel")
  BookingResponse cancel(@PathVariable String reference) {
    return bookingService.cancel(reference);
  }
}
