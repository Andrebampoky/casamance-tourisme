package sn.casamance.reservation;
import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.client.RestClient; import org.springframework.beans.factory.annotation.Value; import java.time.LocalDateTime;
@Component class NotificationRelay {
 private final ReservationEventOutboxRepository events; private final RestClient client;
 NotificationRelay(ReservationEventOutboxRepository events,RestClient.Builder builder,@Value("${app.notification-service-url:http://localhost:8083}") String url){this.events=events;this.client=builder.baseUrl(url).build();}
 @Scheduled(fixedDelayString="${app.notification-relay-interval-ms:5000}") @Transactional void relay(){for(var e:events.findTop20ByStatusAndNextAttemptAtBeforeOrderByIdAsc("PENDING",LocalDateTime.now()))try{if("BOOKING_CONFIRMED".equals(e.getEventType())) client.post().uri("/internal/notifications/booking-confirmed").body(new Confirmed(e.getBookingReference(),e.getRecipient(),e.getCustomerName(),e.getActivity(),e.getLocation(),e.getVisitDate(),e.getParticipants(),e.getTotal())).retrieve().toBodilessEntity(); else client.post().uri("/internal/notifications/booking-cancelled/{ref}",e.getBookingReference()).retrieve().toBodilessEntity();e.sent();}catch(Exception ex){e.failed();}}
 record Confirmed(String bookingReference,String recipient,String customerName,String activity,String location,java.time.LocalDate visitDate,int participants,java.math.BigDecimal total){}
}
