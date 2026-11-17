package sn.casamance.reservation;

import jakarta.persistence.*;

@Embeddable
public class SurveyAnswer {
  private Long questionId;
  @Column(nullable=false, length=240) private String questionPrompt;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private SurveyQuestionType type;
  @Column(nullable=false, length=1000) private String value;

  protected SurveyAnswer() {}
  SurveyAnswer(SurveyQuestion question, String value) {
    this.questionId = question.getId();
    this.questionPrompt = question.getPrompt();
    this.type = question.getType();
    this.value = value.trim();
  }
  Long getQuestionId() { return questionId; }
  String getQuestionPrompt() { return questionPrompt; }
  SurveyQuestionType getType() { return type; }
  String getValue() { return value; }
}
