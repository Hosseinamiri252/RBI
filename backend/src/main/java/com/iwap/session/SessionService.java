package com.iwap.session;

import com.iwap.audit.AuditService;
import com.iwap.docker.DockerService;
import com.iwap.exception.ResourceNotFoundException;
import com.iwap.policy.PolicyService;
import com.iwap.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SessionService {

    @Autowired private DockerService dockerService;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private AuditService auditService;
    @Autowired private PolicyService policyService;

    @Value("${guacd.host}") private String guacdHost;
    @Value("${guacd.port}") private int guacdPort;
    @Value("${recording.path}") private String recordingBasePath;

    @Transactional
    public BrowserSession createSession(User user, String clientIp, String userAgent) {
        policyService.validateCanStartSession(user);

        BrowserSession session = new BrowserSession();
        session.setUser(user);
        session.setClientIp(clientIp);
        session.setUserAgent(userAgent);
        session.setStatus(SessionStatus.STARTING);
        session = sessionRepository.save(session);

        try {
            int vncPort = dockerService.findFreePort();
            String containerName = "iwap-browser-" + session.getId();
            String containerId = dockerService.startBrowserContainer(containerName, vncPort);

            session.setContainerId(containerId);
            session.setContainerName(containerName);
            session.setVncPort(vncPort);
            session.setStatus(SessionStatus.ACTIVE);

            String recordingPath = recordingBasePath + "/" + session.getId();
            session.setRecordingPath(recordingPath);
            session = sessionRepository.save(session);

            auditService.log(user, "SESSION_CREATED", "session", session.getId(), null);
            return session;
        } catch (Exception e) {
            session.setStatus(SessionStatus.ERROR);
            sessionRepository.save(session);
            throw e;
        }
    }

    public GuacamoleTunnel connectTunnel(BrowserSession session) throws GuacamoleException {
        GuacamoleConfiguration config = buildGuacamoleConfig(session);
        GuacamoleSocket socket = new InetGuacamoleSocket(guacdHost, guacdPort);
        ConfiguredGuacamoleSocket configured = new ConfiguredGuacamoleSocket(socket, config);
        return new SimpleGuacamoleTunnel(configured);
    }

    public GuacamoleConfiguration buildGuacamoleConfig(BrowserSession session) {
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol("vnc");
        config.setParameter("hostname", session.getContainerName());
        config.setParameter("port", "5900");
        config.setParameter("password", "iwap_vnc_secret");
        config.setParameter("autoretry", "3");
        config.setParameter("recording-path", session.getRecordingPath());
        config.setParameter("recording-name", session.getId().toString());
        config.setParameter("create-recording-path", "true");
        config.setParameter("recording-exclude-output", "false");
        config.setParameter("recording-exclude-mouse", "false");
        return config;
    }

    @Transactional
    public void closeSession(UUID sessionId, User requestingUser) {
        BrowserSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.getStatus() == SessionStatus.CLOSED) return;

        session.setStatus(SessionStatus.CLOSED);
        session.setEndedAt(Instant.now());
        sessionRepository.save(session);

        if (session.getContainerId() != null) {
            dockerService.stopAndRemoveContainer(session.getContainerId());
        }

        auditService.log(requestingUser, "SESSION_CLOSED", "session", sessionId, null);
    }

    public BrowserSession getSessionForUser(UUID sessionId, User user) {
        BrowserSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!session.getUser().getId().equals(user.getId()) &&
            user.getRole() != com.iwap.user.UserRole.ADMIN) {
            throw new com.iwap.exception.PolicyViolationException("Access denied");
        }
        return session;
    }

    public List<BrowserSession> getUserSessions(UUID userId) {
        return sessionRepository.findByUserId(userId);
    }

    public BrowserSession findById(UUID id) {
        return sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    public Page<BrowserSession> findAll(Pageable pageable) {
        return sessionRepository.findAll(pageable);
    }

    @Scheduled(fixedDelay = 60000)
    public void enforceSessionTimeouts() {
        List<BrowserSession> activeSessions = sessionRepository.findByStatus(SessionStatus.ACTIVE);
        for (BrowserSession session : activeSessions) {
            int maxMinutes = policyService.getMaxSessionDurationMinutes(session.getUser());
            Instant expiry = session.getStartedAt().plus(maxMinutes, ChronoUnit.MINUTES);
            if (Instant.now().isAfter(expiry)) {
                log.info("Auto-closing timed-out session: {}", session.getId());
                closeSession(session.getId(), session.getUser());
            }
        }
    }
}
