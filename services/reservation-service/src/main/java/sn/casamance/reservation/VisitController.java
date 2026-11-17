package sn.casamance.reservation;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@RestController @RequestMapping("/api/visits")
class VisitController {
 private final VisitRepository visits; private final BookingRepository bookings;
 VisitController(VisitRepository visits,BookingRepository bookings){this.visits=visits;this.bookings=bookings;}
 @GetMapping List<Visit> search(@RequestParam(required=false) String q,@RequestParam(required=false) String category,@RequestParam(required=false) LocalDate date){
  String keyword=blankToNull(q);
  String selectedCategory=blankToNull(category);
  List<Visit> result=visits.findAll().stream()
      .filter(visit->keyword==null || (visit.getTitle()+" "+visit.getLocation()+" "+visit.getDescription()).toLowerCase().contains(keyword.toLowerCase()))
      .filter(visit->selectedCategory==null || visit.getCategory().equalsIgnoreCase(selectedCategory))
      .collect(Collectors.toList());
  if(date==null)return result;
  return result.stream().filter(visit->bookings.reservedTravelers(visit.getId(),date)<visit.getCapacity()).collect(Collectors.toList());
 }
 @GetMapping("/{id}/availability") boolean availability(@PathVariable Long id,@RequestParam LocalDate date,@RequestParam(defaultValue="1") int travelers){Visit v=visits.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));return travelers>0 && bookings.reservedTravelers(id,date)+travelers<=v.getCapacity();}
 @GetMapping("/{id}") Visit get(@PathVariable Long id){return visits.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));}
 private String blankToNull(String value){return value==null||value.isBlank()?null:value.trim();}
}
