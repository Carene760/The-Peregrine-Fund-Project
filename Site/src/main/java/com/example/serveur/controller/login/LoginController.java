package com.example.serveur.controller.login;

import com.example.serveur.model.User;
import com.example.serveur.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        User user = userService.findByEmail(email).orElse(null);

        if (user != null && user.getMotDePasse().equals(password)) {
            session.setAttribute("currentUser", user); // <-- stocke dans la session
            return "redirect:/history";
        } else {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
            return "login";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // supprime toutes les données de la session
        return "redirect:/"; // redirige vers la page de login
    }


}

