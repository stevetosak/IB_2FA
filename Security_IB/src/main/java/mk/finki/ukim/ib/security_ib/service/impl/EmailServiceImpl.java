package mk.finki.ukim.ib.security_ib.service.impl;

import mk.finki.ukim.ib.security_ib.entities.AuthToken;
import mk.finki.ukim.ib.security_ib.entities.User;
import mk.finki.ukim.ib.security_ib.service.EmailService;
import mk.finki.ukim.ib.security_ib.service.TokenService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TokenService tokenService;

    private final String verifyURL = "http://localhost:3000/verify";

    public EmailServiceImpl(JavaMailSender mailSender, TokenService tokenService) {
        this.mailSender = mailSender;
        this.tokenService = tokenService;
    }


    @Override
    public void sendVerificationEmailRegister(User user) {

        AuthToken authToken = tokenService.createAuthToken(user,"link", LocalDateTime.now().plusMinutes(5));
        String link = verifyURL + "?token=" + authToken.getTokenValue();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify account registration");
        message.setText("Click on this link to verify your account registration: " + link);
        mailSender.send(message);
    }

    @Override
    public void sendOTPMail(User user) {

        AuthToken authToken = tokenService.createAuthToken(user,"otp", LocalDateTime.now().plusMinutes(5));

        String text = "Your OTP code is: " + authToken.getTokenValue();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("OTP verification");
        message.setText(text);

        mailSender.send(message);

    }
}
