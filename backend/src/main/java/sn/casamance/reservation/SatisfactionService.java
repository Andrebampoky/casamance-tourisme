package sn.casamance.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service
class SatisfactionService {
  private final BookingRepository bookings;
  private final SatisfactionSurveyRepository surveys;
  private final SurveyQuestionRepository questions;

  SatisfactionService(BookingRepository bookings, SatisfactionSurveyRepository surveys, SurveyQuestionRepository questions) {
    this.bookings = bookings;
    this.surveys = surveys;
    this.questions = questions;
  }

  @Transactional
  List<SurveyQuestionResponse> questionsFor(String reference) {
    Booking booking = loadCompletedBooking(reference);
    if (surveys.findByBooking_Reference(booking.getReference()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "L’enquête a déjà été envoyée pour cette réservation");
    }
    return questions.findByActiveTrueOrderByDisplayOrderAsc().stream().map(SurveyQuestionResponse::from).toList();
  }

  @Transactional
  SatisfactionResult submit(String reference, SurveySubmissionRequest request) {
    Booking booking = bookings.findByReferenceForUpdate(reference)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation inconnue"));
    booking.completeIfPast(LocalDate.now());
    if (booking.getStatus() != BookingStatus.COMPLETED) throw new ResponseStatusException(HttpStatus.CONFLICT, "L’enquête est disponible après la visite");
    if (surveys.findByBooking_Reference(reference).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Une seule réponse est autorisée par visite");

    List<SurveyQuestion> active = questions.findByActiveTrueOrderByDisplayOrderAsc();
    Map<Long, SurveyQuestion> byId = new HashMap<>();
    active.forEach(question -> byId.put(question.getId(), question));
    Map<Long, String> submitted = new HashMap<>();
    for (SurveyAnswerRequest answer : request.answers()) {
      if (submitted.put(answer.questionId(), answer.value()) != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question répétée");
      SurveyQuestion question = byId.get(answer.questionId());
      if (question == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question inconnue ou inactive");
      validateAnswer(question, answer.value());
    }
    if (submitted.size() != active.size()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Répondez à toutes les questions actives");
    List<SurveyAnswer> answers = active.stream().map(question -> new SurveyAnswer(question, submitted.get(question.getId()))).toList();
    SatisfactionSurvey survey = surveys.save(new SatisfactionSurvey(booking, answers));
    return new SatisfactionResult(booking.getReference(), answers.size(), survey.getSubmittedAt());
  }

  @Transactional
  List<SurveyQuestionResponse> allQuestions() {
    return questions.findAll().stream().sorted(Comparator.comparingInt(SurveyQuestion::getDisplayOrder)).map(SurveyQuestionResponse::from).toList();
  }

  @Transactional
  SurveyQuestionResponse addQuestion(SurveyQuestionRequest request) {
    if (request.type() == SurveyQuestionType.CHOICE && (request.options() == null || request.options().isBlank())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les options sont requises pour une question à choix");
    }
    return SurveyQuestionResponse.from(questions.save(new SurveyQuestion(request.prompt(), request.type(), request.options(), request.displayOrder())));
  }

  @Transactional
  void setQuestionActive(Long id, boolean active) {
    SurveyQuestion question = questions.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question inconnue"));
    question.setActive(active);
  }

  @Transactional(readOnly = true)
  List<SatisfactionSummary> statistics(Long visitId, LocalDate from, LocalDate to) {
    if (from != null && to != null && from.isAfter(to)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début doit précéder la date de fin");
    Map<Long, SurveyQuestion> questionById = new HashMap<>();
    questions.findAll().forEach(question -> questionById.put(question.getId(), question));
    Map<StatsKey, List<SurveyAnswer>> answersByGroup = new HashMap<>();
    Map<Long, String> activityNames = new HashMap<>();
    surveys.findAll().forEach(survey -> {
      Booking booking = survey.getBooking();
      LocalDate visitDate = booking.getVisitDate();
      if (visitId != null && !visitId.equals(booking.getVisit().getId())) return;
      if (from != null && visitDate.isBefore(from)) return;
      if (to != null && visitDate.isAfter(to)) return;
      activityNames.put(booking.getVisit().getId(), booking.getVisit().getTitle());
      String period = from == null && to == null ? "Toutes périodes" : java.time.YearMonth.from(visitDate).toString();
      survey.getAnswers().forEach(answer -> answersByGroup.computeIfAbsent(
          new StatsKey(answer.getQuestionId(), booking.getVisit().getId(), period), ignored -> new ArrayList<>()).add(answer));
    });
    return answersByGroup.entrySet().stream().map(entry -> {
      List<SurveyAnswer> answers = entry.getValue();
      SurveyQuestion question = questionById.get(entry.getKey().questionId());
      Map<String, Long> counts = new TreeMap<>();
      answers.forEach(answer -> counts.merge(answer.getValue(), 1L, Long::sum));
      Double average = question != null && question.getType() == SurveyQuestionType.SCALE
          ? answers.stream().mapToInt(answer -> Integer.parseInt(answer.getValue())).average().orElse(0)
          : null;
      String prompt = answers.get(0).getQuestionPrompt();
      return new SatisfactionSummary(entry.getKey().questionId(), prompt, entry.getKey().visitId(),
          activityNames.get(entry.getKey().visitId()), entry.getKey().period(), answers.size(), average, counts);
    }).sorted(Comparator.comparing(SatisfactionSummary::activity).thenComparing(SatisfactionSummary::period)
        .thenComparing(SatisfactionSummary::questionId)).toList();
  }

  private Booking loadCompletedBooking(String reference) {
    Booking booking = bookings.findByReferenceForUpdate(reference)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation inconnue"));
    booking.completeIfPast(LocalDate.now());
    if (booking.getStatus() != BookingStatus.COMPLETED) throw new ResponseStatusException(HttpStatus.CONFLICT, "L’enquête est disponible après la visite");
    return booking;
  }

  private void validateAnswer(SurveyQuestion question, String rawValue) {
    String value = rawValue == null ? "" : rawValue.trim();
    boolean valid = switch (question.getType()) {
      case SCALE -> value.matches("[1-5]");
      case YES_NO -> value.equalsIgnoreCase("oui") || value.equalsIgnoreCase("non") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
      case CHOICE -> Arrays.stream(question.getOptions().split("\\|", -1)).map(String::trim).anyMatch(option -> option.equalsIgnoreCase(value));
      case TEXT -> !value.isBlank() && value.length() <= 1000;
    };
    if (!valid) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Réponse invalide pour la question " + question.getId());
  }

  private record StatsKey(Long questionId, Long visitId, String period) {}
}

record SatisfactionResult(String reservationReference, int answersSaved, java.time.LocalDateTime submittedAt) {}
