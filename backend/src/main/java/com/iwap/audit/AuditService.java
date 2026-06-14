package com.iwap.audit;

import com.iwap.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    @Autowired private AuditRepository auditRepository;

    public void log(User user, String action, String resourceType, UUID resourceId,
                    Map<String, Object> details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setDetails(details);
        auditRepository.save(log);
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return auditRepository.findAll(pageable);
    }

    public Page<AuditLog> findByUser(UUID userId, Pageable pageable) {
        return auditRepository.findByUserId(userId, pageable);
    }
}
