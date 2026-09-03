package com.example.userSecurity.auth;

import com.example.userSecurity.jwk.RsaKeyProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
public class TokenController {
    private final AuthenticationManager authenticationManager;
    private final RsaKeyProperties rsaKeyProperties;
    private final PasswordEncoder passwordEncoder;

    private final long accessTokenValiditySeconds = 60 * 60; // 1 hour

    public TokenController(AuthenticationManager authenticationManager, RsaKeyProperties rsaKeyProperties, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.rsaKeyProperties = rsaKeyProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Data
    public static class TokenRequest {

        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@RequestBody TokenRequest tokenRequest) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(tokenRequest.getUsername(), tokenRequest.getPassword());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "invalid_grant", "error_description", "Bad credentials"));
        }

        UserDetails principle = (UserDetails) authentication.getPrincipal();

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenValiditySeconds);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(principle.getUsername())
                .issuer("http://localhost:8080")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .claim("authorities", principle.getAuthorities().stream().map(Object::toString).toList())
                .build();

        try {
            JWSSigner signer = new RSASSASigner(rsaKeyProperties.getPrivateKey());
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(signer);
            String tokenString = signedJWT.serialize();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("access_token", tokenString);
            response.put("token_type", "Bearer");
            response.put("expires_in", accessTokenValiditySeconds);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "server_error", "error_description", e.getMessage()));
        }
    }
}
