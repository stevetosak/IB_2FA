package mk.finki.ukim.ib.security_ib.service.impl;

import jakarta.transaction.Transactional;
import mk.finki.ukim.ib.security_ib.entities.AuthToken;
import mk.finki.ukim.ib.security_ib.entities.User;
import mk.finki.ukim.ib.security_ib.exceptions.InvalidOTPLength;
import mk.finki.ukim.ib.security_ib.exceptions.TokenExpiredException;
import mk.finki.ukim.ib.security_ib.exceptions.TokenNotFoundException;
import mk.finki.ukim.ib.security_ib.repository.TokenRepository;
import mk.finki.ukim.ib.security_ib.repository.UserRepository;
import mk.finki.ukim.ib.security_ib.service.TokenService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;


    public TokenServiceImpl(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void save(AuthToken token) {
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public void verify(String tokenValue) {
        Optional<AuthToken> tokenOptional = tokenRepository.findByTokenValue(tokenValue);
        LocalDateTime now = LocalDateTime.now();

        if (tokenOptional.isEmpty()) {
            throw new TokenNotFoundException("Token " + tokenValue + " not found");
        }

        AuthToken token = tokenOptional.get();

        if (now.isAfter(token.getExpiresAt())) {
            throw new TokenExpiredException("Token expired");
        }
        ;

        User user = token.getUser();

        token.setUsed(true);
        user.setAccountVerified(true);

        tokenRepository.save(token);
        userRepository.save(user);
    }

    @Override
    public String generateMailCode() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String generate2FACode() {
        SecureRandom sr = new SecureRandom();
        int code = sr.nextInt(900000) + 100000;
        return String.format("%06d", code);
    }

    @Override @Transactional
    public void verifyOTP(String[] otp) {
        if (otp.length != 6) {
            throw new InvalidOTPLength("Please fill out all fields");
        }

        String otpConcat = String.join("", otp);
        System.out.println("concat " + otpConcat);

        Optional<AuthToken> authTokenOptional = tokenRepository.findByTokenValue(otpConcat);
        if (authTokenOptional.isEmpty()) {
            throw new TokenNotFoundException("Token does not match");
        }

        AuthToken authToken = authTokenOptional.get();

        if (authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token expired");
        }

        authToken.setUsed(true);

        tokenRepository.save(authToken);

    }

    @Override
    public AuthToken createAuthToken(User user, String type, LocalDateTime expiresAt) {
        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setExpiresAt(expiresAt);

        if (type.equals("otp")) {
            token.setType("otp");
            token.setTokenValue(generate2FACode());
        } else if (type.equals("link")) {
            token.setType("link");
            token.setTokenValue(generateMailCode());
        } else {
            System.out.println("Unsupported token type: " + type);
        }

        return tokenRepository.save(token);
    }
}
