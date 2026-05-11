package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(Map.of(
                "message", "Admin Dashboard",
                "totalUsers", users.size(),
                "users", users
        ));
    }
}
