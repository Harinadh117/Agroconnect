package com.agroconnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agroconnect.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body,
                                       HttpServletRequest request, HttpServletResponse response) {
        try {
            authService.register(body);

            String phone = body.get("phone");
            String password = body.get("password");
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phone, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response);

            return ResponseEntity.ok(authService.getCurrentUser(phone));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                    HttpServletRequest request, HttpServletResponse response) {
        try {
            String phone = body.get("phone");
            String password = body.get("password");

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phone, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response);

            return ResponseEntity.ok(authService.getCurrentUser(phone));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid phone or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(authService.getCurrentUser(auth.getName()));
    }
}
