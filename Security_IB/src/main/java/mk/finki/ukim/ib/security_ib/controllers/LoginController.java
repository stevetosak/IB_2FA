package mk.finki.ukim.ib.security_ib.controllers;

import jakarta.servlet.http.HttpSession;
import mk.finki.ukim.ib.security_ib.entities.User;
import mk.finki.ukim.ib.security_ib.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLoginSubmit(@RequestParam String usernameOrEmail, @RequestParam String password, Model model) {
        try{
            User u = userService.login(usernameOrEmail, password);
            String params = "?uid=" + u.getId();
            return "redirect:/login/otp" + params;
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "login";
        }

    }

    @GetMapping("/login/otp")
    public String otp(Model model, @RequestParam int uid) {
        model.addAttribute("uid", uid);
        return "otp-form";
    }

    @GetMapping("/logout")
    public String logout(Model model, HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
