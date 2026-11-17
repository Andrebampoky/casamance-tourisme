package sn.casamance.reservation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
class SatisfactionSurvey {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) @JoinColumn(nullable=false, unique=true) private Booking booking;
  @Column(nullable=false) private LocalDateTime submittedAt = LocalDateTime.now();
  @ElementCollection(fetch=FetchType.EAGER)
  @CollectionTable(name="satisfaction_answers", joinColumns=@JoinColumn(name="survey_id"))
  @OrderColumn(name="answer_index")
  private List<SurveyAnswer> answers = new ArrayList<>();

  protected SatisfactionSurvey() {}
  SatisfactionSurvey(Booking booking, List<SurveyAnswer> answers) {
    if (booking == null || booking.getStatus() != BookingStatus.COMPLETED) throw new IllegalArgumentException("La visite doit être terminée");
    this.booking = booking;
    this.answers = new ArrayList<>(answers);
  }
  Long getId() { return id; }
  Booking getBooking() { return booking; }
  LocalDateTime getSubmittedAt() { return submittedAt; }
  List<SurveyAnswer> getAnswers() { return List.copyOf(answers); }
}
