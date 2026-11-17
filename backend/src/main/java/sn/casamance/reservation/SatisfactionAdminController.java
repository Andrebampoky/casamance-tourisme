package sn.casamance.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/admin/satisfaction")
class SatisfactionAdminController {
  private final SatisfactionService satisfaction;
  private final AdminAccess adminAccess;
  SatisfactionAdminController(SatisfactionService satisfaction, AdminAccess adminAccess) {
    this.satisfaction = satisfaction;
    this.adminAccess = adminAccess;
  }

  @GetMapping("/questions")
  List<SurveyQuestionResponse> questions(@RequestHeader(value="X-Admin-Token", required=false) String token) {
    adminAccess.require(token);
    return satisfaction.allQuestions();
  }

  @PostMapping("/questions")
  @ResponseStatus(HttpStatus.CREATED)
  SurveyQuestionResponse addQuestion(@RequestHeader(value="X-Admin-Token", required=false) String token,
      @Valid @RequestBody SurveyQuestionRequest request) {
    adminAccess.require(token);
    return satisfaction.addQuestion(request);
  }

  @PatchMapping("/questions/{id}/active")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void setQuestionActive(@RequestHeader(value="X-Admin-Token", required=false) String token,
      @PathVariable Long id, @RequestParam boolean active) {
    adminAccess.require(token);
    satisfaction.setQuestionActive(id, active);
  }

  @GetMapping("/statistics")
  List<SatisfactionSummary> statistics(@RequestHeader(value="X-Admin-Token", required=false) String token,
      @RequestParam(required=false) Long visitId,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
    adminAccess.require(token);
    return satisfaction.statistics(visitId, from, to);
  }
}
