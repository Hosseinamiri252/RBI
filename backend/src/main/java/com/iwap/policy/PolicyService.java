package com.iwap.policy;

import com.iwap.exception.PolicyViolationException;
import com.iwap.exception.ResourceNotFoundException;
import com.iwap.session.SessionRepository;
import com.iwap.session.SessionStatus;
import com.iwap.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PolicyService {

    @Autowired private PolicyRepository policyRepository;
    @Autowired private SessionRepository sessionRepository;

    public void validateCanStartSession(User user) {
        List<AccessPolicy> policies = policyRepository.findByAppliesToRoleAndEnabledTrue(user.getRole());
        int activeSessions = sessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE).size();

        int maxAllowed = user.getMaxConcurrentSessions();
        for (AccessPolicy policy : policies) {
            maxAllowed = Math.min(maxAllowed, policy.getMaxConcurrentSessions());
        }

        if (activeSessions >= maxAllowed) {
            throw new PolicyViolationException(
                "Maximum concurrent sessions reached (" + maxAllowed + ")");
        }
    }

    public int getMaxSessionDurationMinutes(User user) {
        List<AccessPolicy> policies = policyRepository.findByAppliesToRoleAndEnabledTrue(user.getRole());
        int maxDuration = Integer.MAX_VALUE;
        for (AccessPolicy policy : policies) {
            maxDuration = Math.min(maxDuration, policy.getMaxSessionDurationMinutes());
        }
        return maxDuration == Integer.MAX_VALUE ? 480 : maxDuration;
    }

    public Page<AccessPolicy> findAll(Pageable pageable) {
        return policyRepository.findAll(pageable);
    }

    public AccessPolicy create(AccessPolicy policy) {
        return policyRepository.save(policy);
    }

    public AccessPolicy update(UUID id, AccessPolicy updated) {
        AccessPolicy policy = policyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
        policy.setName(updated.getName());
        policy.setDescription(updated.getDescription());
        policy.setAppliesToRole(updated.getAppliesToRole());
        policy.setMaxSessionDurationMinutes(updated.getMaxSessionDurationMinutes());
        policy.setAllowedUrlPatterns(updated.getAllowedUrlPatterns());
        policy.setBlockedUrlPatterns(updated.getBlockedUrlPatterns());
        policy.setMaxConcurrentSessions(updated.getMaxConcurrentSessions());
        policy.setRecordingEnabled(updated.isRecordingEnabled());
        policy.setEnabled(updated.isEnabled());
        return policyRepository.save(policy);
    }
}
