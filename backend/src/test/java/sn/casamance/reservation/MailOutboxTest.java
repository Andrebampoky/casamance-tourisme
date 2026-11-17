package sn.casamance.reservation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MailOutboxTest {
  @Test void retriesDeliveryWithBackoffAndStopsAfterFiveAttempts() {
    MailOutbox outbox = new MailOutbox("reservation-1", "visitor@example.sn");
    for (int attempt = 0; attempt < 4; attempt++) {
      outbox.failed("SMTP indisponible");
      assertEquals("PENDING", outbox.getStatus());
      assertEquals(attempt + 1, outbox.getAttempts());
    }
    outbox.failed("Adresse refusée");
    assertEquals("FAILED", outbox.getStatus());
    assertEquals(5, outbox.getAttempts());
  }

  @Test void cancellationSuppressesAnUnsentConfirmationMessage() {
    MailOutbox outbox = new MailOutbox("reservation-2", "visitor@example.sn");
    outbox.cancelIfPending();
    assertEquals("CANCELLED", outbox.getStatus());
  }

  @Test void cancellationDoesNotRewriteAnEmailAlreadySent() {
    MailOutbox outbox = new MailOutbox("reservation-3", "visitor@example.sn");
    outbox.sent();
    outbox.cancelIfPending();
    assertEquals("SENT", outbox.getStatus());
  }
}
