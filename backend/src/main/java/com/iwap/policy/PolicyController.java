package com.iwap.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/policies")
@PreAuthorize("hasRole('ADMIN')")
public class PolicyController {

    @Autowired private PolicyService policyService;

    @GetMapping
    public Page<AccessPolicy> list(Pageable pageable) {
        return policyService.findAll(pageable);
    }

    @PostMapping
    public AccessPolicy create(@RequestBody AccessPolicy policy) {
        return policyService.create(policy);
    }

    @PutMapping("/{id}")
    public AccessPolicy update(@PathVariable UUID id, @RequestBody AccessPolicy policy) {
        return policyService.update(id, policy);
    }
}
