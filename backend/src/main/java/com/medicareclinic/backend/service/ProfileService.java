package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.ChangePasswordRequest;
import com.medicareclinic.backend.dto.ProfileResponse;
import com.medicareclinic.backend.dto.UpdateProfileRequest;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentProfile(Authentication authentication) {
        return toProfileResponse(getAuthenticatedUser(authentication));
    }

    @Transactional
    public ProfileResponse updateCurrentProfile(Authentication authentication, UpdateProfileRequest request) {
        User currentUser = getAuthenticatedUser(authentication);
        String newUsername = request.username().trim();

        if (!Objects.equals(currentUser.getUsername(), newUsername)
                && userRepository.existsByUsername(newUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        currentUser.setUsername(newUsername);
        User savedUser = userRepository.save(currentUser);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .toList();

        UsernamePasswordAuthenticationToken updatedAuthentication =
                new UsernamePasswordAuthenticationToken(savedUser.getUsername(), authentication.getCredentials(), authorities);
        updatedAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);

        return toProfileResponse(savedUser);
    }

    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        User currentUser = getAuthenticatedUser(authentication);

        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        if (request.newPassword() == null || request.newPassword().trim().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 6 characters");
        }

        if (passwordEncoder.matches(request.newPassword(), currentUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }

        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
        // We keep the current session authenticated; user can remain logged in.
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> getAllProfiles() {
        return userRepository.findAll().stream()
                .map(this::toProfileResponse)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private ProfileResponse toProfileResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .sorted()
                .collect(Collectors.toList());

        return new ProfileResponse(user.getId(), user.getUsername(), user.isEnabled(), roles);
    }
}


