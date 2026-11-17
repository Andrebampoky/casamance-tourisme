package sn.casamance.reservation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController class ReservationHealthController { @GetMapping("/internal/health") String health(){return "UP";} }
