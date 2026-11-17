package sn.casamance.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
  List<SurveyQuestion> findByActiveTrueOrderByDisplayOrderAsc();
}
