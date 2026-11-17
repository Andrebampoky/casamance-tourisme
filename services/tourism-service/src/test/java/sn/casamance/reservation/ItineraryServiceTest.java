package sn.casamance.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItineraryServiceTest {
  private final ReservationCatalogClient catalog = mock(ReservationCatalogClient.class);
  private final ItineraryRepository itineraries = mock(ItineraryRepository.class);
  private final ItineraryService service = new ItineraryService(catalog, itineraries);
  private final LocalDate start = LocalDate.now().plusDays(3);

  @Test void createsItineraryWithinBudgetFromMatchingCatalogVisits() {
    VisitSnapshot river = snapshot(1L, "Balade en pirogue sur le fleuve Casamance", "Nature", "1h 30", "12000");
    VisitSnapshot forest = snapshot(2L, "Forêt de Bayotte", "Nature", "2h", "8000");
    when(catalog.visits()).thenReturn(List.of(river, forest));
    when(catalog.available(anyLong(), any(LocalDate.class), anyInt())).thenReturn(true);
    when(itineraries.save(any())).thenAnswer(call -> call.getArgument(0));

    ItineraryResponse result = service.generate(new ItineraryRequest(List.of("NATURE"), start, 1, 2, new BigDecimal("25000")));

    assertEquals(1, result.stops().size());
    assertEquals("Balade en pirogue sur le fleuve Casamance", result.stops().get(0).getTitle());
    assertEquals(new BigDecimal("24000"), result.estimatedTotal());
    assertEquals(new BigDecimal("1000"), result.budgetRemaining());
    assertEquals("XOF", result.currency());
  }

  @Test void returnsAnEmptySavedPlanWhenBudgetCannotCoverAnyActivity() {
    when(catalog.visits()).thenReturn(List.of(snapshot(1L, "Forêt de Bayotte", "Nature", "2h", "8000")));
    when(catalog.available(anyLong(), any(LocalDate.class), anyInt())).thenReturn(true);
    when(itineraries.save(any())).thenAnswer(call -> call.getArgument(0));

    ItineraryResponse result = service.generate(new ItineraryRequest(List.of("NATURE"), start, 2, 1, BigDecimal.ZERO));

    assertTrue(result.stops().isEmpty());
    assertEquals(BigDecimal.ZERO, result.estimatedTotal());
  }

  @Test void rejectsUnknownInterestCodes() {
    assertThrows(ResponseStatusException.class, () -> service.generate(
        new ItineraryRequest(List.of("UNKNOWN"), start, 1, 1, BigDecimal.TEN)));
  }

  private VisitSnapshot snapshot(Long id, String title, String category, String duration, String price) {
    return new VisitSnapshot(id, title, "Découverte de la nature et de la biodiversité locale.", "Ziguinchor", category,
        duration, new BigDecimal(price), "river", 4.8, 12);
  }
}
