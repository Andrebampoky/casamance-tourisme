package sn.casamance.notification;
import org.junit.jupiter.api.Test; import java.math.BigDecimal; import java.time.LocalDate; import static org.junit.jupiter.api.Assertions.*;
class MailOutboxTest {
 private MailOutbox create(){return new MailOutbox(new BookingConfirmedNotification("ref","a@b.sn","Awa","Visite","Ziguinchor",LocalDate.now(),2,new BigDecimal("20000")));}
 @Test void retriesDeliveryWithBackoff(){MailOutbox m=create();for(int i=1;i<=5;i++){m.failed("SMTP down");assertEquals(i,m.getAttempts());}assertEquals("FAILED",m.getStatus());}
 @Test void cancellationOnlySuppressesPendingMessage(){MailOutbox m=create();m.cancelIfPending();assertEquals("CANCELLED",m.getStatus());MailOutbox sent=create();sent.sent();sent.cancelIfPending();assertEquals("SENT",sent.getStatus());}
}
