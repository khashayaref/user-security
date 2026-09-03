package com.example.userSecurity.dataLoader;


import com.example.userSecurity.entity.User;
import com.example.userSecurity.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String username = ""; // put whatever you want
        String password = ""; // put whatever you want
        String passwordHash = passwordEncoder.encode(password);

        try{
            User user = userService.register(username, passwordHash, "ROLE_USER");
            System.out.println("Created demo user: " + user + "/ password (hashed stored)");
        } catch (Exception e){
            System.out.println("Error creating demo user: " + e.getMessage());
        }
    }
}
