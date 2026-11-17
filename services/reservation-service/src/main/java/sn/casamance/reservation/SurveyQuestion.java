package sn.casamance.reservation;

import jakarta.persistence.*;

@Entity
class SurveyQuestion {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, length=240) private String prompt;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private SurveyQuestionType type;
  @Column(length=1000) private String options;
  @Column(nullable=false) private int displayOrder;
  @Column(nullable=false) private boolean active = true;

  protected SurveyQuestion() {}
  SurveyQuestion(String prompt, SurveyQuestionType type, String options, int displayOrder) {
    this.prompt = prompt.trim();
    this.type = type;
    this.options = options == null ? "" : options.trim();
    this.displayOrder = displayOrder;
    if (type == SurveyQuestionType.CHOICE && this.options.isBlank()) throw new IllegalArgumentException("Les options sont requises");
  }
  void setActive(boolean active) { this.active = active; }
  Long getId() { return id; }
  String getPrompt() { return prompt; }
  SurveyQuestionType getType() { return type; }
  String getOptions() { return options; }
  int getDisplayOrder() { return displayOrder; }
  boolean isActive() { return active; }
}
