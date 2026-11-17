package sn.casamance.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class ItineraryService {
  private static final int MAX_DAILY_MINUTES = 8 * 60;
  private static final List<InterestOption> INTERESTS = List.of(
      new InterestOption("NATURE", "Nature et écotourisme", "Forêts, fleuve, faune et flore.", "♧"),
      new InterestOption("CULTURE", "Culture et traditions", "Rencontres, traditions et vie locale.", "♬"),
      new InterestOption("HERITAGE", "Patrimoine historique", "Monuments et histoire de la région.", "⌂"),
      new InterestOption("BEACH", "Plages et détente", "Littoral, plage et activités nautiques.", "☼"),
      new InterestOption("FOOD", "Gastronomie locale", "Saveurs et spécialités de Casamance.", "◇"),
      new InterestOption("CRAFT", "Artisanat", "Savoir-faire et créations locales.", "✳"),
      new InterestOption("VILLAGES", "Découverte des villages", "Découverte des localités et de leurs habitants.", "⌖"),
      new InterestOption("WILDLIFE", "Faune et flore", "Biodiversité et paysages naturels.", "♧"),
      new InterestOption("WATER", "Activités nautiques", "Fleuve, bolongs et littoral.", "≈"),
      new InterestOption("FESTIVAL", "Festivals et événements", "Manifestations culturelles et temps forts.", "▣"));

  private final VisitRepository visits;
  private final BookingRepository bookings;
  private final ItineraryRepository itineraries;

  ItineraryService(VisitRepository visits, BookingRepository bookings, ItineraryRepository itineraries) {
    this.visits = visits;
    this.bookings = bookings;
    this.itineraries = itineraries;
  }

  List<InterestOption> interests() { return INTERESTS; }

  @Transactional
  ItineraryResponse generate(ItineraryRequest request) {
    List<String> selected = request.interests().stream().map(code -> code.trim().toUpperCase(Locale.ROOT)).distinct().toList();
    for (String code : selected) {
      if (INTERESTS.stream().noneMatch(option -> option.code().equals(code))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Centre d’intérêt inconnu : " + code);
      }
    }
    List<Visit> candidates = visits.findAll().stream()
        .filter(visit -> interestScore(visit, selected) > 0)
        .sorted(Comparator.<Visit>comparingInt(visit -> interestScore(visit, selected)).reversed()
            .thenComparing(Visit::getPrice).thenComparing(Visit::getTitle))
        .toList();

    BigDecimal remaining = request.budget();
    BigDecimal total = BigDecimal.ZERO;
    int dailyMinutes = 0;
    int stopsToday = 0;
    int day = 1;
    LocalDate date = request.startDate();
    Set<Long> usedVisits = new HashSet<>();
    List<ItineraryStop> stops = new ArrayList<>();
    while (day <= request.durationDays()) {
      boolean addedToday = false;
      for (Visit visit : candidates) {
        if (usedVisits.contains(visit.getId())) continue;
        BigDecimal cost = visit.getPrice().multiply(BigDecimal.valueOf(request.travelers()));
        int duration = durationInMinutes(visit.getDuration());
        if (stopsToday >= 3 || dailyMinutes + duration > MAX_DAILY_MINUTES || cost.compareTo(remaining) > 0) continue;
        if (bookings.reservedTravelers(visit.getId(), date) + request.travelers() > visit.getCapacity()) continue;
        stops.add(new ItineraryStop(day, stopsToday + 1, visit, cost));
        usedVisits.add(visit.getId());
        remaining = remaining.subtract(cost);
        total = total.add(cost);
        dailyMinutes += duration;
        stopsToday++;
        addedToday = true;
        break;
      }
      if (!addedToday || stopsToday >= 3 || dailyMinutes >= MAX_DAILY_MINUTES) {
        day++;
        date = date.plusDays(1);
        dailyMinutes = 0;
        stopsToday = 0;
      }
    }
    Itinerary itinerary = itineraries.save(new Itinerary(request.startDate(), request.durationDays(),
        request.travelers(), request.budget(), selected, total, stops));
    return ItineraryResponse.from(itinerary);
  }

  @Transactional(readOnly = true)
  ItineraryResponse get(String reference) {
    return itineraries.findByReference(reference).map(ItineraryResponse::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Itinéraire inconnu"));
  }

  private int interestScore(Visit visit, List<String> selected) {
    String text = normalize(visit.getTitle() + " " + visit.getCategory() + " " + visit.getDescription() + " " + visit.getLocation());
    return (int) selected.stream().flatMap(code -> keywords(code).stream()).map(ItineraryService::normalize).filter(text::contains).distinct().count();
  }

  private List<String> keywords(String code) {
    return switch (code) {
      case "NATURE" -> List.of("nature", "ecotourisme", "foret", "faune", "flore", "biodiversite", "fleuve", "pirogu");
      case "CULTURE" -> List.of("culture", "tradition", "village", "artisanat", "gastronomie", "festival", "ceremonie");
      case "HERITAGE" -> List.of("patrimoine", "historique", "histoire", "monument", "cathedrale");
      case "BEACH" -> List.of("plage", "detente", "littoral", "mer", "cap skirring");
      case "FOOD" -> List.of("gastronomie", "cuisine", "saveur", "restaurant");
      case "CRAFT" -> List.of("artisanat", "artisan", "savoir-faire", "creation");
      case "VILLAGES" -> List.of("village", "localite", "riverain", "rencontre");
      case "WILDLIFE" -> List.of("faune", "flore", "biodiversite", "foret", "nature");
      case "WATER" -> List.of("nautique", "fleuve", "pirogu", "bolong", "plage", "littoral");
      case "FESTIVAL" -> List.of("festival", "evenement", "culturel", "manifestation");
      default -> List.of(code.toLowerCase(Locale.ROOT));
    };
  }

  private static String normalize(String value) {
    return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
  }

  private int durationInMinutes(String value) {
    Matcher hours = Pattern.compile("(\\d+)\\s*h(?:\\s*(\\d+))?", Pattern.CASE_INSENSITIVE).matcher(value);
    if (hours.find()) return Integer.parseInt(hours.group(1)) * 60 + (hours.group(2) == null ? 0 : Integer.parseInt(hours.group(2)));
    Matcher minutes = Pattern.compile("(\\d+)\\s*(?:min|m)", Pattern.CASE_INSENSITIVE).matcher(value);
    return minutes.find() ? Integer.parseInt(minutes.group(1)) : 120;
  }
}
