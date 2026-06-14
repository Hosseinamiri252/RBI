package com.iwap.user;

import com.iwap.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User createLocalUser(String username, String password, String displayName,
                                 String email, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setRole(role);
        user.setAuthSource(AuthSource.LOCAL);
        return userRepository.save(user);
    }

    @Transactional
    public User syncLdapUser(String username) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setDisplayName(username);
            user.setRole(UserRole.USER);
            user.setAuthSource(AuthSource.LDAP);
            return userRepository.save(user);
        });
    }

    @Transactional
    public User update(UUID id, String displayName, String email, UserRole role,
                        int maxConcurrentSessions) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setRole(role);
        user.setMaxConcurrentSessions(maxConcurrentSessions);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleEnabled(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void updateLastLogin(UUID id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setLastLoginAt(Instant.now());
            userRepository.save(u);
        });
    }
}
