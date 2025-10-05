package com.pradeep.papertrail.controller;

import com.pradeep.papertrail.model.User;
import com.pradeep.papertrail.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // Registration endpoint
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // hash password
        userRepository.save(user);
        return "User registered successfully!";
    }

    // Login endpoint
    @PostMapping("/login")
    public String login(@RequestBody User user, HttpSession session) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );

        if (authentication.isAuthenticated()) {
            // manually set authentication in Spring Security context
            org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            // Spring will create a JSESSIONID automatically
            session.setAttribute("SPRING_SECURITY_CONTEXT",
                    org.springframework.security.core.context.SecurityContextHolder.getContext());

            return "Login successful! Session ID: " + session.getId();
        } else {
            return "Invalid credentials!";
        }
    }


    @GetMapping("/session-info")
    public String sessionInfo(HttpSession session, Authentication auth) {
        if (auth == null) {
            return "No authenticated user!";
        }

        return "Session ID: " + session.getId() +
                ", User: " + auth.getName() +
                ", Created: " + new Date(session.getCreationTime()) +
                ", Last accessed: " + new Date(session.getLastAccessedTime());
    }

}
