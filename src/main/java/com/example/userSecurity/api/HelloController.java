package com.example.userSecurity.api;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal(expression = "claims['sub']") String username) {
        return Map.of("hello", username);
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is public if you configured permitAll, else protected.";
    }
}
