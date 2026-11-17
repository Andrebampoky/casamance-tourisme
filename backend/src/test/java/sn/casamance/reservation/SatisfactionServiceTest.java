package sn.casamance.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SatisfactionServiceTest {
  private final BookingRepository bookings = mock(BookingRepository.class);
  private final SatisfactionSurveyRepository surveys = mock(SatisfactionSurveyRepository.class);
  private final SurveyQuestionRepository questions = mock(SurveyQuestionRepository.class);
  private final SatisfactionService service = new SatisfactionService(bookings, surveys, questions);

  @Test void acceptsOneTypedResponseForACompletedVisit() {
    Booking booking = completedBooking();
    SurveyQuestion rating = question(1L, "Accueil ?", SurveyQuestionType.SCALE, "");
    SurveyQuestion comment = question(2L, "Commentaire ?", SurveyQuestionType.TEXT, "");
    when(bookings.findByReferenceForUpdate(booking.getReference())).thenReturn(Optional.of(booking));
    when(questions.findByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(rating, comment));
    when(surveys.save(any())).thenAnswer(call -> call.getArgument(0));

    SatisfactionResult result = service.submit(booking.getReference(), new SurveySubmissionRequest(List.of(
        new SurveyAnswerRequest(1L, "5"), new SurveyAnswerRequest(2L, "Très bonne visite"))));

    assertEquals(2, result.answersSaved());
    verify(surveys).save(any(SatisfactionSurvey.class));
  }

  @Test void rejectsMultipleValuesForTheSameQuestion() {
    Booking booking = completedBooking();
    SurveyQuestion rating = question(1L, "Accueil ?", SurveyQuestionType.SCALE, "");
    when(bookings.findByReferenceForUpdate(booking.getReference())).thenReturn(Optional.of(booking));
    when(questions.findByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(rating));

    assertThrows(ResponseStatusException.class, () -> service.submit(booking.getReference(),
        new SurveySubmissionRequest(List.of(new SurveyAnswerRequest(1L, "4"), new SurveyAnswerRequest(1L, "5")))));
    verify(surveys, never()).save(any());
  }

  @Test void aggregatesSatisfactionByActivityAndVisitMonth() {
    Booking booking = completedBooking();
    SurveyQuestion rating = question(1L, "Accueil ?", SurveyQuestionType.SCALE, "");
    SatisfactionSurvey survey = new SatisfactionSurvey(booking, List.of(new SurveyAnswer(rating, "4"), new SurveyAnswer(rating, "5")));
    when(surveys.findAll()).thenReturn(List.of(survey));
    when(questions.findAll()).thenReturn(List.of(rating));

    List<SatisfactionSummary> results = service.statistics(23L, LocalDate.now().minusDays(3), LocalDate.now());

    assertEquals(1, results.size());
    assertEquals("Pirogue", results.get(0).activity());
    assertEquals(2, results.get(0).answerCount());
    assertEquals(4.5, results.get(0).averageScore());
    assertEquals(LocalDate.now().minusDays(1).getYear() + "-" + String.format("%02d", LocalDate.now().minusDays(1).getMonthValue()), results.get(0).period());
  }

  private Booking completedBooking() {
    Visit visit = new Visit("Pirogue", "Balade", "Ziguinchor", "Nature", "1h", new BigDecimal("1000"), "river", 4.8, 10);
    ReflectionTestUtils.setField(visit, "id", 23L);
    Booking booking = new Booking(visit, "Awa", "awa@example.sn", LocalDate.now().plusDays(1), 1);
    booking.confirm();
    ReflectionTestUtils.setField(booking, "visitDate", LocalDate.now().minusDays(1));
    booking.completeIfPast(LocalDate.now());
    return booking;
  }

  private SurveyQuestion question(long id, String prompt, SurveyQuestionType type, String options) {
    SurveyQuestion question = new SurveyQuestion(prompt, type, options, (int) id);
    ReflectionTestUtils.setField(question, "id", id);
    return question;
  }
}
