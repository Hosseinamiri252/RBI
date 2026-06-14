package com.iwap.session;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<BrowserSession, UUID> {
    List<BrowserSession> findByUserIdAndStatus(UUID userId, SessionStatus status);
    List<BrowserSession> findByUserId(UUID userId);
    Page<BrowserSession> findAll(Pageable pageable);
    long countByStatus(SessionStatus status);
    List<BrowserSession> findByStatus(SessionStatus status);
}
