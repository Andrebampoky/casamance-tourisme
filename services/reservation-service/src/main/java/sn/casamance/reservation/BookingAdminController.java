package sn.casamance.reservation;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
class BookingAdminController {
  private final BookingService bookings;
  private final AdminAccess adminAccess;

  BookingAdminController(BookingService bookings, AdminAccess adminAccess) {
    this.bookings = bookings;
    this.adminAccess = adminAccess;
  }

  @GetMapping
  List<BookingResponse> list(@RequestHeader(value="X-Admin-Token", required=false) String token,
      @RequestParam(defaultValue="PENDING") BookingStatus status) {
    adminAccess.require(token);
    return bookings.listForAdmin(status);
  }

  @PostMapping("/{reference}/confirm")
  BookingResponse confirm(@PathVariable String reference, @RequestHeader(value="X-Admin-Token", required=false) String token) {
    adminAccess.require(token);
    return bookings.confirm(reference);
  }
}
