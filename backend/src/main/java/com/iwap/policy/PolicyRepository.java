package com.iwap.policy;

import com.iwap.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<AccessPolicy, UUID> {
    List<AccessPolicy> findByAppliesToRoleAndEnabledTrue(UserRole role);
    Page<AccessPolicy> findAll(Pageable pageable);
}
