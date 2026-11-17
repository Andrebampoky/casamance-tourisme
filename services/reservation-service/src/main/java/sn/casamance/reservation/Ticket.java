package sn.casamance.reservation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
class Ticket {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true, updatable=false, length=36) private String token;
  @OneToOne(optional=false) @JoinColumn(nullable=false, unique=true) private Booking booking;
  @Column(nullable=false) private String status = "VALID";
  @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();

  protected Ticket() {}

  Ticket(Booking booking) {
    if (booking == null || booking.getStatus() != BookingStatus.CONFIRMED) {
      throw new IllegalArgumentException("Un billet ne peut être émis que pour une réservation confirmée");
    }
    this.booking = booking;
    this.token = UUID.randomUUID().toString();
  }

  void revoke() { status = "REVOKED"; }
  Long getId() { return id; }
  String getToken() { return token; }
  Booking getBooking() { return booking; }
  String getStatus() { return status; }
  LocalDateTime getCreatedAt() { return createdAt; }
}
