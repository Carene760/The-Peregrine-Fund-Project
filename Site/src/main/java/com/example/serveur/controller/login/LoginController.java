package com.example.serveur.controller.login;

import com.example.serveur.model.User;
import com.example.serveur.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LoginController {

    private static final String REMEMBER_USER_COOKIE = "rememberUserEmail";

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String showLoginForm(HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/history";
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            HttpServletResponse response,
                            Model model) {
        String normalizedEmail = email == null ? "" : email.replaceAll("\\s+", "");
        User user = userService.findByEmail(normalizedEmail).orElse(null);

        if (user != null && user.getMotDePasse().equals(password)) {
            session.setAttribute("currentUser", user); // <-- stocke dans la session

            Cookie rememberCookie = new Cookie(
                    REMEMBER_USER_COOKIE,
                    URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
            );
            rememberCookie.setHttpOnly(true);
            rememberCookie.setPath("/");
            rememberCookie.setMaxAge(60 * 60 * 24 * 30); // 30 jours
            response.addCookie(rememberCookie);

            return "redirect:/history";
        } else {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
            model.addAttribute("submittedEmail", normalizedEmail);
            model.addAttribute("submittedPassword", password == null ? "" : password);
            return "login";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate(); // supprime toutes les données de la session

        Cookie removeCookie = new Cookie(REMEMBER_USER_COOKIE, "");
        removeCookie.setPath("/");
        removeCookie.setMaxAge(0);
        response.addCookie(removeCookie);

        return "redirect:/"; // redirige vers la page de login
    }


}

