package com.iwap.auth;

import com.iwap.user.User;
import com.iwap.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = (User) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);
        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                "role", user.getRole()
            )
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
            "role", user.getRole(),
            "authSource", user.getAuthSource(),
            "maxConcurrentSessions", user.getMaxConcurrentSessions()
        ));
    }

    record LoginRequest(String username, String password) {}
}
