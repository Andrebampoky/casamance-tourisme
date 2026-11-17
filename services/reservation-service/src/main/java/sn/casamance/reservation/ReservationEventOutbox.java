package sn.casamance.reservation;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(uniqueConstraints=@UniqueConstraint(columnNames={"bookingReference","eventType"}))
class ReservationEventOutbox {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String bookingReference; @Column(nullable=false) private String eventType;
 @Column(nullable=false) private String recipient; private String customerName; private String activity; private String location; private java.time.LocalDate visitDate; private int participants; private java.math.BigDecimal total;
 @Column(nullable=false) private String status="PENDING"; @Column(nullable=false) private int attempts=0; @Column(nullable=false) private LocalDateTime nextAttemptAt=LocalDateTime.now();
 protected ReservationEventOutbox(){}
 static ReservationEventOutbox confirmed(Booking b){return create(b,"BOOKING_CONFIRMED");}
 static ReservationEventOutbox cancelled(Booking b){return create(b,"BOOKING_CANCELLED");}
 private static ReservationEventOutbox create(Booking b,String type){var e=new ReservationEventOutbox();e.bookingReference=b.getReference();e.eventType=type;e.recipient=b.getEmail();e.customerName=b.getCustomerName();e.activity=b.getVisit().getTitle();e.location=b.getVisit().getLocation();e.visitDate=b.getVisitDate();e.participants=b.getTravelers();e.total=b.getTotal();return e;}
 boolean isPending(){return "PENDING".equals(status);} void cancelIfPending(){if(isPending())status="CANCELLED";} void sent(){status="SENT";} void failed(){attempts++;nextAttemptAt=LocalDateTime.now().plusSeconds(Math.min(3600, 5L << Math.min(attempts,9)));}
 Long getId(){return id;} String getBookingReference(){return bookingReference;} String getEventType(){return eventType;} String getRecipient(){return recipient;} String getCustomerName(){return customerName;} String getActivity(){return activity;} String getLocation(){return location;} java.time.LocalDate getVisitDate(){return visitDate;} int getParticipants(){return participants;} java.math.BigDecimal getTotal(){return total;}
}
