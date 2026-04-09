package com.example.serveur.controller;

import com.example.serveur.model.Patrouilleurs;
import com.example.serveur.model.User;
import com.example.serveur.model.UserApp;
import com.example.serveur.service.PatrouilleurService;
import com.example.serveur.service.UserAppService;
import com.example.serveur.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminListController {

    private final UserService userService;
    private final PatrouilleurService patrouilleurService;
    private final UserAppService userAppService;

    @Autowired
    public AdminListController(UserService userService, PatrouilleurService patrouilleurService, UserAppService userAppService) {
        this.userService = userService;
        this.patrouilleurService = patrouilleurService;
        this.userAppService = userAppService;
    }

    @GetMapping("/userslist")
    public String showUsersList(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/";
        }

        List<User> users = userService.findAll();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", users);
        return "userslist";
    }

    @GetMapping("/agentslist")
    public String showAgentsList(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/";
        }

        List<Patrouilleurs> agents = patrouilleurService.findAll();
        Map<Integer, String> agentLogins = userAppService.findAll().stream()
                .filter(userApp -> userApp.getPatrouilleur() != null)
                .collect(Collectors.toMap(
                        userApp -> userApp.getPatrouilleur().getIdPatrouilleur(),
                        UserApp::getLogin,
                        (existing, replacement) -> existing
                ));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("agents", agents);
        model.addAttribute("agentLogins", agentLogins);
        return "agentslist";
    }
}
