package com.example.serveur.config;

import com.example.serveur.model.User;
import com.example.serveur.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class RememberUserInterceptor implements HandlerInterceptor {

    private static final String REMEMBER_USER_COOKIE = "rememberUserEmail";

    private final UserService userService;

    public RememberUserInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();

        if (isPublicPath(requestUri, contextPath)) {
            return true;
        }

        HttpSession session = request.getSession();
        if (session.getAttribute("currentUser") != null) {
            return true;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return true;
        }

        for (Cookie cookie : cookies) {
            if (!REMEMBER_USER_COOKIE.equals(cookie.getName())) {
                continue;
            }

            String email = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            User user = userService.findByEmail(email).orElse(null);
            if (user != null) {
                session.setAttribute("currentUser", user);
                return true;
            }
            break;
        }

        try {
            response.sendRedirect(contextPath + "/login");
            return false;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean isPublicPath(String requestUri, String contextPath) {
        String path = requestUri;
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            path = requestUri.substring(contextPath.length());
        }

        return "/".equals(path)
                || "/login".equals(path)
                || "/logout".equals(path)
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/api/")
                || path.startsWith("/sync/")
                || path.startsWith("/error")
                || "/favicon.ico".equals(path);
    }
}
