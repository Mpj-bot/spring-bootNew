package com.example.springdemo.controller;

import com.example.springdemo.modal.Modal;
import com.example.springdemo.services.EmailAlreadyExistsException;
import com.example.springdemo.services.InvalidCredentialsException;
import com.example.springdemo.services.Services;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@org.springframework.stereotype.Controller
public class Controller {

    private final Services services;

    public Controller(Services services) {
        this.services = services;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/home")
    public String welcome(HttpSession session, Model model) {
        Object userEmail = session.getAttribute("userEmail");
        if (!(userEmail instanceof String email) || email.isBlank()) {
            return "redirect:/";
        }

        model.addAttribute("userEmail", email);
        return "home";
    }

    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "false") boolean terms) {
        String normalizedName = name.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (normalizedName.isEmpty() || normalizedName.length() > 100
                || normalizedEmail.length() > 254 || !normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || !terms) {
            return ResponseEntity.badRequest().body(Map.of("message", "Check the form fields and accept the terms."));
        }

        try {
            services.registerUser(normalizedName, normalizedEmail, password, true);
        } catch (EmailAlreadyExistsException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", exception.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Account created successfully."));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request) {
        try {
            Modal user = services.authenticateUser(email, password);
            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", user.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "You are signed in.",
                    "email", user.getEmail(),
                    "redirect", "/home"
            ));
        } catch (InvalidCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
