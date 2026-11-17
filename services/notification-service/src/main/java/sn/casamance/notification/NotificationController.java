package sn.casamance.notification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/internal/notifications")
class NotificationController {
 private final MailOutboxRepository outbox;
 NotificationController(MailOutboxRepository outbox){this.outbox=outbox;}
 @PostMapping("/booking-confirmed") @ResponseStatus(HttpStatus.ACCEPTED)
 void confirmed(@RequestBody BookingConfirmedNotification event){outbox.findByBookingReference(event.bookingReference()).orElseGet(()->outbox.save(new MailOutbox(event)));}
 @PostMapping("/booking-cancelled/{reference}") @ResponseStatus(HttpStatus.NO_CONTENT)
 void cancelled(@PathVariable String reference){outbox.findByBookingReference(reference).ifPresent(MailOutbox::cancelIfPending);}
 @GetMapping("/health") String health(){return "UP";}
}
