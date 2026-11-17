package sn.casamance.reservation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Booking {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(unique=true, updatable=false, length=36) private String reference;
  @ManyToOne(optional=false) private Visit visit;
  @Column(nullable=false) private String customerName;
  @Column(nullable=false) private String email;
  @Column(nullable=false) private LocalDate visitDate;
  @Column(nullable=false) private int travelers;
  @Column(nullable=false, precision=12, scale=2) private BigDecimal total;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private BookingStatus status=BookingStatus.PENDING;
  @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
  protected Booking() {}
  public Booking(Visit visit, String customerName, String email, LocalDate visitDate, int travelers) {
    if (visit == null) throw new IllegalArgumentException("Une activité est obligatoire");
    if (customerName == null || customerName.isBlank() || customerName.length() > 120) throw new IllegalArgumentException("Le nom est invalide");
    if (email == null || email.isBlank()) throw new IllegalArgumentException("L’adresse e-mail est obligatoire");
    if (visitDate == null || visitDate.isBefore(LocalDate.now())) throw new IllegalArgumentException("La date doit être aujourd’hui ou dans le futur");
    if (travelers < 1 || travelers > 20) throw new IllegalArgumentException("Le nombre de participants doit être compris entre 1 et 20");
    this.reference=UUID.randomUUID().toString(); this.visit=visit; this.customerName=customerName.trim(); this.email=email.trim(); this.visitDate=visitDate; this.travelers=travelers; this.total=visit.getPrice().multiply(BigDecimal.valueOf(travelers));
  }
  public void confirm() {
    if (status != BookingStatus.PENDING) throw new IllegalStateException("Seule une demande en attente peut être confirmée");
    status = BookingStatus.CONFIRMED;
  }
  public void cancel(LocalDate today) {
    if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) throw new IllegalStateException("Cette réservation ne peut plus être annulée");
    if (!visitDate.isAfter(today.plusDays(1))) throw new IllegalStateException("L’annulation doit être faite au moins un jour avant la visite");
    status = BookingStatus.CANCELLED;
  }
  public boolean completeIfPast(LocalDate today) {
    if (status == BookingStatus.CONFIRMED && visitDate.isBefore(today)) {
      status = BookingStatus.COMPLETED;
      return true;
    }
    return false;
  }
  public Long getId(){return id;} public String getReference(){return reference;} public Visit getVisit(){return visit;} public String getCustomerName(){return customerName;} public String getEmail(){return email;} public LocalDate getVisitDate(){return visitDate;} public int getTravelers(){return travelers;} public BigDecimal getTotal(){return total;} public BookingStatus getStatus(){return status;} public LocalDateTime getCreatedAt(){return createdAt;}
}
