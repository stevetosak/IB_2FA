package mk.finki.ukim.ib.security_ib.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomePageController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("isLoggedIn",false);
        return "home";
    }
}
