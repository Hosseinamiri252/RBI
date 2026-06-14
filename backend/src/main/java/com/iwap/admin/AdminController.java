package com.iwap.admin;

import com.iwap.audit.AuditLog;
import com.iwap.audit.AuditService;
import com.iwap.session.BrowserSession;
import com.iwap.session.SessionService;
import com.iwap.session.SessionRepository;
import com.iwap.session.SessionStatus;
import com.iwap.user.User;
import com.iwap.user.UserRepository;
import com.iwap.recording.RecordingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private SessionService sessionService;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private AuditService auditService;
    @Autowired private UserRepository userRepository;
    @Autowired private RecordingRepository recordingRepository;

    @GetMapping("/sessions")
    public Page<BrowserSession> sessions(Pageable pageable) {
        return sessionService.findAll(pageable);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> forceClose(@PathVariable UUID id,
                                         @AuthenticationPrincipal User admin) {
        sessionService.closeSession(id, admin);
        return ResponseEntity.ok(Map.of("message", "Session force-closed"));
    }

    @GetMapping("/audit")
    public Page<AuditLog> auditLogs(Pageable pageable) {
        return auditService.findAll(pageable);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        long activeSessions = sessionRepository.countByStatus(SessionStatus.ACTIVE);
        long totalUsers = userRepository.count();
        long totalRecordings = recordingRepository.count();
        return Map.of(
            "activeSessions", activeSessions,
            "totalUsers", totalUsers,
            "totalRecordings", totalRecordings
        );
    }
}
