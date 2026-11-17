package sn.casamance.reservation;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

record TourismEventRequest(
    @NotBlank @Size(max=180) String title,
    @NotBlank @Size(max=2000) String description,
    @NotBlank @Size(max=80) String type,
    @NotBlank @Size(max=160) String location,
    @NotNull @FutureOrPresent LocalDateTime startsAt,
    @NotNull LocalDateTime endsAt,
    @Size(max=1000) String practicalInfo,
    @NotBlank @Size(max=500) String imageUrl) {}
