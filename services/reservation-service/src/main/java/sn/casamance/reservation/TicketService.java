package sn.casamance.reservation;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
class TicketService {
  private final TicketRepository tickets;
  private final String publicUrl;

  TicketService(TicketRepository tickets, @Value("${app.public-url:http://localhost:8080}") String publicUrl) {
    this.tickets = tickets;
    this.publicUrl = publicUrl.replaceAll("/$", "");
  }

  Ticket issue(Booking booking) {
    return tickets.save(new Ticket(booking));
  }

  @Transactional(readOnly = true)
  TicketResponse findForReservation(String reference) {
    Ticket ticket = tickets.findByBooking_Reference(reference)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun billet confirmé pour cette réservation"));
    Booking booking = ticket.getBooking();
    String verifyUrl = publicUrl + "/api/tickets/verify/" + ticket.getToken();
    return new TicketResponse(booking.getReference(), booking.getVisit().getTitle(), booking.getVisit().getLocation(),
        booking.getVisitDate(), booking.getTravelers(), booking.getTotal(), ticket.getStatus(), qrDataUri(verifyUrl));
  }

  @Transactional(readOnly = true)
  TicketValidation verify(String token) {
    return tickets.findByToken(token)
        .map(ticket -> new TicketValidation("VALID".equals(ticket.getStatus()), ticket.getStatus(),
            ticket.getBooking().getVisit().getTitle(), ticket.getBooking().getVisitDate()))
        .orElseGet(() -> new TicketValidation(false, "UNKNOWN", "", null));
  }

  private String qrDataUri(String value) {
    try {
      BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 280, 280);
      StringBuilder svg = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 280 280\"><rect width=\"280\" height=\"280\" fill=\"white\"/><path fill=\"#101713\" d=\"");
      for (int y = 0; y < 280; y++) {
        for (int x = 0; x < 280; x++) {
          if (matrix.get(x, y)) svg.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
        }
      }
      svg.append("\"/></svg>");
      return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.toString().getBytes(StandardCharsets.UTF_8));
    } catch (WriterException error) {
      throw new IllegalStateException("Impossible de générer le QR code du billet", error);
    }
  }
}
