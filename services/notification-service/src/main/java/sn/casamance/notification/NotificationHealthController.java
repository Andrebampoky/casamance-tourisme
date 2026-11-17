package sn.casamance.notification;
import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.RestController;
@RestController class NotificationHealthController { @GetMapping("/internal/health") String health(){return "UP";} }
