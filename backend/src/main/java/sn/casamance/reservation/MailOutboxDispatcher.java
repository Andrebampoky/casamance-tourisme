package sn.casamance.reservation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
class MailOutboxDispatcher {
  private static final Logger log = LoggerFactory.getLogger(MailOutboxDispatcher.class);
  private final MailOutboxRepository outbox;
  private final BookingRepository bookings;
  private final ObjectProvider<JavaMailSender> senderProvider;
  private final String mailHost;
  private final String senderAddress;
  private final String frontendUrl;

  MailOutboxDispatcher(MailOutboxRepository outbox, BookingRepository bookings,
      ObjectProvider<JavaMailSender> senderProvider,
      @Value("${spring.mail.host:}") String mailHost,
      @Value("${app.mail.from:no-reply@casamance.local}") String senderAddress,
      @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
    this.outbox = outbox;
    this.bookings = bookings;
    this.senderProvider = senderProvider;
    this.mailHost = mailHost;
    this.senderAddress = senderAddress;
    this.frontendUrl = frontendUrl.replaceAll("/$", "");
  }

  @Scheduled(fixedDelayString = "${app.mail.dispatch-interval-ms:30000}")
  @Transactional
  void dispatchPending() {
    if (mailHost.isBlank()) return;
    JavaMailSender sender = senderProvider.getIfAvailable();
    if (sender == null) return;
    for (MailOutbox message : outbox.findTop10ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc("PENDING", LocalDateTime.now())) {
      try {
        Booking booking = bookings.findByReference(message.getBookingReference()).orElseThrow();
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(senderAddress);
        mail.setTo(message.getRecipient());
        mail.setSubject("Votre visite en Casamance est confirmée");
        mail.setText("Bonjour " + booking.getCustomerName() + ",\n\nVotre réservation " + booking.getReference()
            + " est confirmée.\nVisite : " + booking.getVisit().getTitle()
            + "\nDate : " + booking.getVisitDate() + "\nParticipants : " + booking.getTravelers()
            + "\nMontant : " + booking.getTotal() + " FCFA\n\nVotre billet et QR code : "
            + frontendUrl + "/?ticketRef=" + booking.getReference()
            + "\n\nMerci de votre confiance. Casamance Tourisme.");
        sender.send(mail);
        message.sent();
      } catch (Exception error) {
        message.failed(error.getMessage());
        log.warn("Échec d’envoi de l’e-mail de réservation {} (tentative {})", message.getBookingReference(), message.getAttempts());
      }
    }
  }
}
