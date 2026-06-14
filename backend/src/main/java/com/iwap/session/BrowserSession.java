package com.iwap.session;

import com.iwap.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "browser_sessions")
@Data
@NoArgsConstructor
public class BrowserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "container_id")
    private String containerId;

    @Column(name = "container_name")
    private String containerName;

    @Column(name = "vnc_port")
    private Integer vncPort;

    @Column(name = "guacamole_connection_id")
    private String guacamoleConnectionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.STARTING;

    @Column(name = "started_at")
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "recording_path")
    private String recordingPath;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "user_agent")
    private String userAgent;
}
