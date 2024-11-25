package mk.finki.ukim.ib.security_ib.controllers;

import jakarta.servlet.http.HttpSession;
import mk.finki.ukim.ib.security_ib.entities.AuthToken;
import mk.finki.ukim.ib.security_ib.entities.User;
import mk.finki.ukim.ib.security_ib.exceptions.TokenException;
import mk.finki.ukim.ib.security_ib.service.TokenService;
import mk.finki.ukim.ib.security_ib.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TokenController {
    private final TokenService tokenService;
    private final UserService userService;

    public TokenController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token) {
        try {
            tokenService.verify(token);
            return "redirect:/login";
        } catch (TokenException e) {
            System.out.println(e.getMessage());
            System.out.println("ERR");
            return "redirect:/register";
        }
    }

    @PostMapping("/verify-otp")
    public String verifyLogin(@RequestParam(value = "otp[]")String[] otp,
                              @RequestParam int uid,
                              Model model,
                              HttpSession session) {

        try{
            tokenService.verifyOTP(otp);
            User user = userService.findById(uid);
            System.out.println("USER " + user);
            session.setAttribute("user",user);
            session.setAttribute("isLoggedIn",true);
            return "redirect:/";


        } catch (Exception e){
            System.out.println(e.getMessage());
            model.addAttribute("isLoggedIn",false);
            model.addAttribute("error",e.getMessage());
            model.addAttribute("uid",uid);
            return "otp-form";
        }





    }

}
