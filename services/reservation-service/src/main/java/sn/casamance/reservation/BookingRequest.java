package sn.casamance.reservation;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record BookingRequest(@NotNull Long visitId, @NotBlank @Size(max=120) String customerName, @NotBlank @Email String email, @NotNull @FutureOrPresent LocalDate visitDate, @Min(1) @Max(20) int travelers) {}
