package sn.casamance.reservation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
@Configuration class DemoData {
 @Bean CommandLineRunner seed(VisitRepository visits){return args->{if(visits.count()==0){
  visits.save(new Visit("Balade en pirogue sur le fleuve Casamance","Une sortie paisible entre mangroves, bolongs et villages riverains.","Ziguinchor","Nature","1h 30",new BigDecimal("12000"),"river",4.9,12));
  visits.save(new Visit("Forêt de Bayotte","Promenade accompagnée au cœur de la forêt et découverte de sa biodiversité.","Enampore","Nature","2h",new BigDecimal("8000"),"forest",4.8,10));
  visits.save(new Visit("Cathédrale Saint-Antoine de Padoue","Découverte de l'architecture et de l'histoire de ce monument de Ziguinchor.","Ziguinchor","Culture","1h",new BigDecimal("5000"),"church",4.7,20));
  visits.save(new Visit("Plage de Cap Skirring","Journée de détente sur le littoral casamançais.","Cap Skirring","Plage","2h",new BigDecimal("10000"),"beach",4.8,16));
 } };}
}
