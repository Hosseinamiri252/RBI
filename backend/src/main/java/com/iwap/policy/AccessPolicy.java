package com.iwap.policy;

import com.iwap.user.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "access_policies")
@Data
@NoArgsConstructor
public class AccessPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to_role")
    private UserRole appliesToRole = UserRole.USER;

    @Column(name = "max_session_duration_minutes")
    private int maxSessionDurationMinutes = 480;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_url_patterns", columnDefinition = "text[]")
    private List<String> allowedUrlPatterns;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "blocked_url_patterns", columnDefinition = "text[]")
    private List<String> blockedUrlPatterns;

    @Column(name = "max_concurrent_sessions")
    private int maxConcurrentSessions = 1;

    @Column(name = "recording_enabled")
    private boolean recordingEnabled = true;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
