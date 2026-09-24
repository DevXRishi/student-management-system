package com.rishi.student_management.controller;

import com.rishi.student_management.dto.AuthRequest;
import com.rishi.student_management.dto.AuthResponse;
import com.rishi.student_management.dto.RefreshTokenRequest;
import com.rishi.student_management.service.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        String token = jwtService.generateToken(
                Map.of("role", role),
                request.getUsername()
        );

        String refreshToken =
                jwtService.generateRefreshToken(
                        request.getUsername()
                );

        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        refreshToken
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        String refreshToken =
                request.getRefreshToken();

        if (!jwtService.validateRefreshToken(refreshToken)) {

            return ResponseEntity.status(401)
                    .body("Invalid or expired refresh token");
        }

        String username =
                jwtService.extractUsername(refreshToken);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        String newAccessToken =
                jwtService.generateToken(
                        Map.of("role", role),
                        username
                );

        return ResponseEntity.ok(
                new AuthResponse(
                        newAccessToken,
                        refreshToken
                )
        );
    }
}