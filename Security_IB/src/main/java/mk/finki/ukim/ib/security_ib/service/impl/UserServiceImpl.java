package mk.finki.ukim.ib.security_ib.service.impl;

import jakarta.transaction.Transactional;
import mk.finki.ukim.ib.security_ib.entities.AuthToken;
import mk.finki.ukim.ib.security_ib.entities.User;
import mk.finki.ukim.ib.security_ib.exceptions.*;
import mk.finki.ukim.ib.security_ib.repository.UserRepository;
import mk.finki.ukim.ib.security_ib.service.EmailService;
import mk.finki.ukim.ib.security_ib.service.TokenService;
import mk.finki.ukim.ib.security_ib.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenService tokenService;

    public UserServiceImpl(UserRepository userRepository, EmailService emailService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    private String getSalt(){
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return new String(Base64.getEncoder().encode(salt));
    }

    @Transactional
    @Override
    public User register(String username, String email, String password) {
        userRepository.findUserByUsername(username).ifPresent(user -> {
            throw new UsernameExistsException("Username " + username + " already exists");
        });
        userRepository.findUserByEmail(email).ifPresent(user -> {
            throw new EmailExistsException("Email " + email + " already exists");
        });

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String salt = getSalt();
        String passwordHash = encoder.encode(password.concat(salt));
        System.out.println("SALT " + salt);

        String saltedPasswordHash = String.format("%s:%s", passwordHash, salt);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(saltedPasswordHash);

        userRepository.save(user);

        emailService.sendVerificationEmailRegister(user);

        return user;
    }

    @Override
    public User findById(int id) {
        Optional<User> user = userRepository.findUserById(id);

        if(user.isEmpty()){
            throw new UserNotFoundException("User with id " + id + " not found");
        }

        return user.get();
    }

    private boolean validate(User user, String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String[] passwordSplit = user.getPassword().split(":");
        String hashedPassword = passwordSplit[0];
        String salt = passwordSplit[passwordSplit.length-1];

        return encoder.matches(password.concat(salt), hashedPassword);

    }

    @Override
    public User login(String usernameOrEmail, String password) {
        User usr;

        Optional<User> usrByUsername = userRepository.findUserByUsername(usernameOrEmail);
        Optional<User> usrByEmail = userRepository.findUserByEmail(usernameOrEmail);

        if(usrByUsername.isPresent()){
            usr = usrByUsername.get();
        } else if (usrByEmail.isPresent()) {
            usr = usrByEmail.get();
        } else {
            throw new UserNotFoundException("Invalid credentials");
        }

        if(!validate(usr, password)){
            throw new IncorrectPasswordException("Incorrect password");
        }

        if(!usr.isAccountVerified()){
            throw new UserNotVerifiedException("This account has not been activated.\nPlease click on the link sent to your email to activate it");
        }

        emailService.sendOTPMail(usr);

        return usr;

    }
}
