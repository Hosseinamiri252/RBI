package com.iwap.session;

import com.iwap.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired private SessionService sessionService;

    @PostMapping
    public BrowserSession create(@AuthenticationPrincipal User user,
                                  HttpServletRequest request) {
        String clientIp = request.getHeader("X-Real-IP");
        if (clientIp == null) clientIp = request.getRemoteAddr();
        return sessionService.createSession(user, clientIp, request.getHeader("User-Agent"));
    }

    @GetMapping
    public List<BrowserSession> list(@AuthenticationPrincipal User user) {
        return sessionService.getUserSessions(user.getId());
    }

    @GetMapping("/{id}")
    public BrowserSession get(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return sessionService.getSessionForUser(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> close(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        sessionService.closeSession(id, user);
        return ResponseEntity.ok(Map.of("message", "Session closed"));
    }
}
