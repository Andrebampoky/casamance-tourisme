package sn.casamance.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/satisfaction/reservations/{reference}")
class SatisfactionController {
  private final SatisfactionService satisfaction;
  SatisfactionController(SatisfactionService satisfaction) { this.satisfaction = satisfaction; }

  @GetMapping("/questions")
  List<SurveyQuestionResponse> questions(@PathVariable String reference) { return satisfaction.questionsFor(reference); }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  SatisfactionResult submit(@PathVariable String reference, @Valid @RequestBody SurveySubmissionRequest request) {
    return satisfaction.submit(reference, request);
  }
}
