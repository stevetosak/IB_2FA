package mk.finki.ukim.ib.security_ib.service;

import mk.finki.ukim.ib.security_ib.entities.AuthToken;
import mk.finki.ukim.ib.security_ib.entities.User;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TokenService {
    void save(AuthToken token);
    void verify(String tokenValue);
    String generateMailCode();
    String generate2FACode();

    void verifyOTP(String[] otp);

    AuthToken createAuthToken(User user, String type, LocalDateTime expiresAt) ;
}
