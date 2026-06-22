package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.RegisterRequest;
import com.medicareclinic.backend.model.Role;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.RoleRepository;
import com.medicareclinic.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Returns current user info, or 401 if not authenticated.
    // Endpoint is permitAll so unauthenticated calls reach here with authentication == null.
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", roles
        ));
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken"));
        }

        Role userRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseThrow(() -> new IllegalStateException("ROLE_PATIENT not found — DataInitializer may not have run"));

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);

        // Use a mutable HashSet — Hibernate cannot manage immutable Set.of() collections
        user.setRoles(new HashSet<>());
        user.getRoles().add(userRole);

        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("username", user.getUsername(), "roles", List.of("ROLE_PATIENT")));
    }
}
