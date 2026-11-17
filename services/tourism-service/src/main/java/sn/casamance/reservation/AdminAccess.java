package sn.casamance.reservation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
class AdminAccess {
  private final String configuredToken;
  AdminAccess(@Value("${app.admin-token:}") String configuredToken) { this.configuredToken = configuredToken; }

  void require(String suppliedToken) {
    if (configuredToken.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "L’accès administrateur n’est pas configuré");
    if (suppliedToken == null || !MessageDigest.isEqual(configuredToken.getBytes(StandardCharsets.UTF_8), suppliedToken.getBytes(StandardCharsets.UTF_8))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
    }
  }
}
