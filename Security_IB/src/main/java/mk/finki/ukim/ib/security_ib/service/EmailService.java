package mk.finki.ukim.ib.security_ib.service;

import mk.finki.ukim.ib.security_ib.entities.User;

public interface EmailService {

    void sendVerificationEmailRegister(User user);
    void sendOTPMail(User user);
}
