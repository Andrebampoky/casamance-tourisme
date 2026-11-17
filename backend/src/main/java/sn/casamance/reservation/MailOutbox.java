package sn.casamance.reservation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
class MailOutbox {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, length=36) private String bookingReference;
  @Column(nullable=false) private String recipient;
  @Column(nullable=false) private String status = "PENDING";
  @Column(nullable=false) private int attempts;
  @Column(nullable=false) private LocalDateTime nextAttemptAt = LocalDateTime.now();
  @Column(length=1000) private String lastError;
  @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();

  protected MailOutbox() {}
  MailOutbox(String bookingReference, String recipient) { this.bookingReference = bookingReference; this.recipient = recipient; }
  void sent() { status = "SENT"; lastError = null; }
  void cancelIfPending() { if ("PENDING".equals(status)) status = "CANCELLED"; }
  void failed(String error) {
    attempts++;
    status = attempts >= 5 ? "FAILED" : "PENDING";
    nextAttemptAt = LocalDateTime.now().plusMinutes(Math.min(60, attempts * attempts * 2L));
    lastError = error == null ? "Échec d’envoi" : error.substring(0, Math.min(error.length(), 1000));
  }
  Long getId() { return id; }
  String getBookingReference() { return bookingReference; }
  String getRecipient() { return recipient; }
  String getStatus() { return status; }
  int getAttempts() { return attempts; }
  LocalDateTime getNextAttemptAt() { return nextAttemptAt; }
}
