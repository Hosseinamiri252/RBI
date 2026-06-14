package com.iwap.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired private UserService userService;

    @GetMapping
    public Page<User> list(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @PostMapping
    public User create(@RequestBody CreateUserRequest req) {
        return userService.createLocalUser(req.username(), req.password(),
            req.displayName(), req.email(), req.role());
    }

    @PutMapping("/{id}")
    public User update(@PathVariable UUID id, @RequestBody UpdateUserRequest req) {
        return userService.update(id, req.displayName(), req.email(),
            req.role(), req.maxConcurrentSessions());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @PatchMapping("/{id}/toggle")
    public User toggle(@PathVariable UUID id) {
        return userService.toggleEnabled(id);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> resetPassword(@PathVariable UUID id,
                                            @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }

    record CreateUserRequest(String username, String password, String displayName,
                              String email, UserRole role) {}

    record UpdateUserRequest(String displayName, String email, UserRole role,
                              int maxConcurrentSessions) {}
}
