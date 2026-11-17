package sn.casamance.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TourismEventServiceTest {
  private final TourismEventRepository repository = mock(TourismEventRepository.class);
  private final TourismEventService service = new TourismEventService(repository);
  private final LocalDate eventDay = LocalDate.of(2026, 12, 12);

  @Test void combinesTypeDatePlaceAndKeywordFilters() {
    TourismEvent festival = new TourismEvent("Festival des cultures", "Rencontre des cultures locales", "Festival",
        "Ziguinchor", eventDay.atTime(10, 0), eventDay.atTime(18, 0), "Entrée libre", "");
    TourismEvent concert = new TourismEvent("Concert du soir", "Musique locale", "Concert",
        "Oussouye", eventDay.atTime(19, 0), eventDay.atTime(22, 0), "", "");
    when(repository.findAll()).thenReturn(List.of(festival, concert));

    List<TourismEventResponse> results = service.search("festival", eventDay, null, null, "ziguinchor", "cultures");

    assertEquals(1, results.size());
    assertEquals("Festival des cultures", results.get(0).title());
  }

  @Test void rejectsReversedDateRange() {
    assertThrows(ResponseStatusException.class,
        () -> service.search(null, null, eventDay.plusDays(1), eventDay, null, null));
  }
}
