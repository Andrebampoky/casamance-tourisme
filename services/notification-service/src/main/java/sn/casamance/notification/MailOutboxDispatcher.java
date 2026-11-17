package sn.casamance.notification;
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
 private static final Logger log=LoggerFactory.getLogger(MailOutboxDispatcher.class);
 private final MailOutboxRepository outbox; private final ObjectProvider<JavaMailSender> senderProvider;
 private final String mailHost,from,frontend;
 MailOutboxDispatcher(MailOutboxRepository outbox,ObjectProvider<JavaMailSender> senderProvider,@Value("${spring.mail.host:}") String mailHost,@Value("${app.mail.from:no-reply@casamance.local}") String from,@Value("${app.frontend-url:http://localhost:4201}") String frontend){this.outbox=outbox;this.senderProvider=senderProvider;this.mailHost=mailHost;this.from=from;this.frontend=frontend.replaceAll("/$","");}
 @Scheduled(fixedDelayString="${app.mail.dispatch-interval-ms:30000}") @Transactional
 void dispatchPending(){
  if(mailHost.isBlank())return; JavaMailSender sender=senderProvider.getIfAvailable();if(sender==null)return;
  for(MailOutbox m:outbox.findTop10ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc("PENDING",LocalDateTime.now()))try{
   SimpleMailMessage mail=new SimpleMailMessage();mail.setFrom(from);mail.setTo(m.getRecipient());mail.setSubject("Votre visite en Casamance est confirmée");
   mail.setText("Bonjour "+m.getCustomerName()+",\n\nVotre réservation "+m.getBookingReference()+" est confirmée.\nVisite : "+m.getActivity()+"\nLieu : "+m.getLocation()+"\nDate : "+m.getVisitDate()+"\nParticipants : "+m.getParticipants()+"\nMontant : "+m.getTotal()+" FCFA\n\nVotre billet : "+frontend+"/?ticketRef="+m.getBookingReference()+"\n\nMerci. Casamance Tourisme.");
   sender.send(mail);m.sent();
  }catch(Exception e){m.failed(e.getMessage());log.warn("Échec du courriel {} (tentative {})",m.getBookingReference(),m.getAttempts());}
 }
}
