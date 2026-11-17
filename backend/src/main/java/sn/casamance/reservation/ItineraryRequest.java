package sn.casamance.reservation;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

record ItineraryRequest(
    @NotEmpty @Size(max=10) List<@NotBlank String> interests,
    @NotNull @FutureOrPresent LocalDate startDate,
    @Min(1) @Max(14) int durationDays,
    @Min(1) @Max(20) int travelers,
    @NotNull @DecimalMin("0.00") @Digits(integer=10, fraction=2) BigDecimal budget) {}
