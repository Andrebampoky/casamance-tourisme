package sn.casamance.reservation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
class SurveyQuestionSeed {
  @Bean CommandLineRunner seedSurveyQuestions(SurveyQuestionRepository questions) {
    return args -> {
      if (questions.count() != 0) return;
      questions.saveAll(List.of(
          new SurveyQuestion("Comment évaluez-vous la qualité de l’accueil ?", SurveyQuestionType.SCALE, "", 1),
          new SurveyQuestion("Comment évaluez-vous l’organisation de la visite ?", SurveyQuestionType.SCALE, "", 2),
          new SurveyQuestion("Les horaires annoncés ont-ils été respectés ?", SurveyQuestionType.YES_NO, "", 3),
          new SurveyQuestion("Quel aspect avez-vous préféré ?", SurveyQuestionType.CHOICE, "Accueil|Découverte|Paysages|Organisation", 4),
          new SurveyQuestion("Que pourrions-nous améliorer ?", SurveyQuestionType.TEXT, "", 5)));
    };
  }
}
