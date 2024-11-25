package mk.finki.ukim.ib.security_ib.controllers;

import mk.finki.ukim.ib.security_ib.entities.User;
import mk.finki.ukim.ib.security_ib.service.EmailService;
import mk.finki.ukim.ib.security_ib.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String register() {
        return "register";
    }

    @PostMapping
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email, Model model) {

        try{
            User user = userService.register(username, email, password);
            String params = "?uid=" + user.getId();
            return "redirect:/mail-verify" + params;
        } catch (Exception e){
            model.addAttribute("error", e.getMessage());
            return "register";
        }

    }

}
