package mk.finki.ukim.ib.security_ib.controllers;

import mk.finki.ukim.ib.security_ib.entities.User;
import mk.finki.ukim.ib.security_ib.service.EmailService;
import mk.finki.ukim.ib.security_ib.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mail-verify")
public class EmailController {
    private final EmailService emailService;
    private final UserService userService;

    public EmailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }


    @GetMapping
    public String getMailVerificationPage(Model model, @RequestParam int uid) {

        model.addAttribute("uid", uid);
        return "verify-account";
    }


    @GetMapping("/resend-vlink")
    public String sendEmail(Model model,@RequestParam int uid) {

       try{
           User user = userService.findById(uid);
           emailService.sendVerificationEmailRegister(user);
       } catch (Exception e) {
           System.out.println(e.getMessage());
       }
       model.addAttribute("uid", uid);
        return "verify-account";
    }

    @GetMapping("/resend-otp")
    public String sendOTP(Model model,@RequestParam int uid) {

        try{
            User user = userService.findById(uid);
            emailService.sendOTPMail(user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        model.addAttribute("uid", uid);
        return "otp-form";
    }
}
