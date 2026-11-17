package sn.casamance.reservation;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

record SurveyQuestionRequest(@NotBlank @Size(max=240) String prompt, @NotNull SurveyQuestionType type,
    @Size(max=1000) String options, @Min(0) int displayOrder) {}

record SurveyQuestionResponse(Long id, String prompt, SurveyQuestionType type, java.util.List<String> options,
    int displayOrder, boolean active) {
  static SurveyQuestionResponse from(SurveyQuestion question) {
    java.util.List<String> options = question.getOptions().isBlank() ? java.util.List.of()
        : java.util.Arrays.stream(question.getOptions().split("\\|", -1)).map(String::trim).toList();
    return new SurveyQuestionResponse(question.getId(), question.getPrompt(), question.getType(), options, question.getDisplayOrder(), question.isActive());
  }
}

record SurveyAnswerRequest(@NotNull Long questionId, @NotBlank @Size(max=1000) String value) {}
record SurveySubmissionRequest(@NotEmpty @Size(max=30) java.util.List<@Valid SurveyAnswerRequest> answers) {}
record SatisfactionSummary(Long questionId, String prompt, Long visitId, String activity, String period,
    long answerCount, Double averageScore, java.util.Map<String, Long> answerCounts) {}
