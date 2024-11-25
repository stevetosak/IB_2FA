package mk.finki.ukim.ib.security_ib.service;

import mk.finki.ukim.ib.security_ib.entities.User;

public interface UserService {
    User register(String username, String email, String password);
    User findById(int id);
    User login(String usernameOrEmail, String password);
}
